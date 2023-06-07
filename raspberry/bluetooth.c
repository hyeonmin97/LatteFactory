/*
추가해야할 내용 : 
*/
//gcc -o bluetooth bluetooth.c -lwiringPi -lbluetooth -lm -lpthread

//자이로센서
#include <wiringPiI2C.h>
#include <wiringPi.h>
#include <stdio.h>
#include <stdlib.h> //abs
#include <math.h>
#include <time.h>
#include <string.h>



//블루투스
#include <sys/socket.h>

#include <unistd.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>
#include <bluetooth/rfcomm.h>


#include <sys/time.h>

//스레드
#include <pthread.h>

//자이로센서
#define Device_Address 0x68
#define PWR_MGMT_1 0x6B
#define SMPLRT_DIV 0x19
#define CONFIG 0x1A
#define GYRO_CONFIG 0x1B
#define INT_ENABLE 0x38
#define ACCEL_XOUT_H 0x3B
#define ACCEL_YOUT_H 0x3D
#define ACCEL_ZOUT_H 0x3F
#define GYRO_XOUT_H 0x43
#define GYRO_YOUT_H 0x45
#define GYRO_ZOUT_H 0x47

//소켓
#define PORT 9000
#define BUF_SIZE 1024

//자이로
float AcX, AcY, AcZ, Tmp, GyX, GyY, GyZ;
float dt;
float accel_angle_x, accel_angle_y, accel_angle_z;
float gyro_angle_x, gyro_angle_y, gyro_angle_z;
float filtered_angle_x, filtered_angle_y, filtered_angle_z;
float baseAcX, baseAcY, baseAcZ;
float baseGyX, baseGyY, baseGyZ;
float gyro_x, gyro_y, gyro_z;
unsigned long now = 0;  // 현재 시간 저장용 변수
unsigned long past = 0; // 이전 시간 저장용 변수
int fd;

void calcDT();
void initMPU6050();
short read_raw_data(int);
void readAccelGyro();
void calibAccelGyro();
void calcAccelYPR();
void calcGyroYPR();
void calcFilteredYPR();
sdp_session_t *register_service(uint8_t);

//스레드
pthread_mutex_t    mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t    cond = PTHREAD_COND_INITIALIZER;


//블루투스
int init_server();
int sock;
void* readGyro(void* arg){
    pthread_t fallen;
    struct timeval before, bgn;
    char readBuf[1024] = { 0 };
    double diff;
    int client = *(int*)arg;
    printf("gyro thread : %d\n", client);
    gettimeofday(&before, NULL);
    while(1){
        //센서값 읽기
        readAccelGyro();
        calcDT();
        calcAccelYPR();
        calcGyroYPR();
        calcFilteredYPR();
        printf("FX : %6.2f | FY : %6.2f | FZ : %6.2f\n", filtered_angle_x, filtered_angle_y, filtered_angle_z);
        //낙상 감지
        if (abs((int)filtered_angle_x) > 40) //x축이 40도보다 크면
        {   
            gettimeofday(&bgn, NULL);
            diff = bgn.tv_sec + bgn.tv_usec / 1000000.0 - before.tv_sec - before.tv_usec / 1000000.0;
            printf("%f\n",diff);
            if(diff <= 5)
                continue;
            else{
                printf("fall detection\n");
                pthread_mutex_lock(&mutex);
                write(client, "fallen", strlen("fallen"));
                pthread_cond_wait(&cond, &mutex);//스레드 대기
                gettimeofday(&before, NULL);
                pthread_mutex_unlock(&mutex);
            }
                
        }
        else
        {
            gettimeofday(&before, NULL);

        }
    }
    printf("readGyro close()\n");
}
int main()
{
    pthread_t gyro;
    int timeToFall = 0;
    fd = wiringPiI2CSetup(Device_Address);
    initMPU6050();
    calibAccelGyro(); // 안정된 상태에서의 가속도 자이로 값 계산

    past = millis();
    

    //블루투스 연결
    while(1){
        printf("init server\n");
        //bluetooth
        int client = init_server();
        char buf[1024] = { 0 };
        char messageArr[1024] = { 0 };

        while(1){
            //첫 연결시 블루투스에서 값 읽음(connect)
            memset(buf, 0, sizeof(buf));
            int read_lenth = read(client, buf, sizeof(buf));
            if(read_lenth>0){
                if(!strcmp("connect", buf)){
                    printf("recv connect\n");
                    int id = pthread_create(&gyro, NULL, readGyro, (void*)&client);//스레드 생성
                    //pthread_join(gyro, NULL);
                    
                } else if(!strcmp("disconnect",buf)){
                    pthread_cancel(gyro);//스레드 종료
                    close(client);
                    close(sock);
                    printf("disconnect out\n");
                    break;
                } else if(!strncmp("btn",buf,3)){
                    pthread_cond_signal(&cond);//스레드 대기 해제
                    printf("button : %s\n", buf);
                }
                printf("main - rec : [%s]\n",buf);
            }
            
            
        }
        printf("out\n");
        
    }
    return 0;
}
void calcDT()
{
    now = millis();
    dt = (now - past) / 1000.0;
    past = now;
}
void initMPU6050()
{
    wiringPiI2CWriteReg8(fd, SMPLRT_DIV, 0x07); /* Write to sample rate register */
    wiringPiI2CWriteReg8(fd, PWR_MGMT_1, 0x01); /* Write to power management register */
    wiringPiI2CWriteReg8(fd, CONFIG, 0); /* Write to Configuration register */           //디지털 필터 사용안함
    wiringPiI2CWriteReg8(fd, GYRO_CONFIG, 8); /* Write to Gyro Configuration register */ //fs_sel = 1 => +- 1000 도/초, 범위가 작으면 섬세하게, 범위가 넓으면 큰 각도변화
    wiringPiI2CWriteReg8(fd, INT_ENABLE, 0x01); /*Write to interrupt enable register */
}
short read_raw_data(int addr)
{
    short high_byte, low_byte, value;
    high_byte = wiringPiI2CReadReg8(fd, addr);
    low_byte = wiringPiI2CReadReg8(fd, addr + 1);
    value = (high_byte << 8) | low_byte;
    return value;
}

void readAccelGyro()
{
    AcX = read_raw_data(ACCEL_XOUT_H);
    AcY = read_raw_data(ACCEL_YOUT_H);
    AcZ = read_raw_data(ACCEL_ZOUT_H);

    GyX = read_raw_data(GYRO_XOUT_H);
    GyY = read_raw_data(GYRO_YOUT_H);
    GyZ = read_raw_data(GYRO_ZOUT_H);
}
void calibAccelGyro()
{
    float sumAcX = 0, sumAcY = 0, sumAcZ = 0;
    float sumGyX = 0, sumGyY = 0, sumGyZ = 0;
    readAccelGyro(0);
    for (int i = 0; i < 10; i++)
    {
        readAccelGyro(0);
        sumAcX += AcX;
        sumAcY += AcY;
        sumAcZ += AcZ;
        sumGyX += GyX;
        sumGyY += GyY;
        sumGyZ += GyZ;
        delay(100);
    }
    baseAcX = sumAcX / 10;
    baseAcY = sumAcY / 10;
    baseAcZ = sumAcZ / 10;
    baseGyX = sumGyX / 10;
    baseGyY = sumGyY / 10;
    baseGyZ = sumGyZ / 10;
    //printf("baseAcX %f\t", baseAcX);
    //printf("baseAcY %f\t", baseAcY);
    //printf("baseAcZ %f\t\n", baseAcZ);
}
void calcAccelYPR()
{
    float accel_x, accel_y, accel_z;
    float accel_xz, accel_yz;
    const float RADIANS_TO_DEGREES = 180 / 3.14159;
    accel_x = AcX - baseAcX;
    accel_y = AcY - baseAcY;
    accel_z = AcZ + (16384 - baseAcZ);

    accel_yz = sqrt(pow(accel_y, 2) + pow(accel_z, 2));
    accel_angle_y = atan(-accel_x / accel_yz) * RADIANS_TO_DEGREES;
    accel_xz = sqrt(pow(accel_x, 2) + pow(accel_z, 2));
    accel_angle_x = atan(accel_y / accel_xz) * RADIANS_TO_DEGREES;
    accel_angle_z = 0;
}
void calcGyroYPR()
{
    const float GYROXYZ_TO_DEGREES_PER_SEC = 65;
    gyro_x = (GyX - baseGyX) / GYROXYZ_TO_DEGREES_PER_SEC;
    gyro_y = (GyY - baseGyY) / GYROXYZ_TO_DEGREES_PER_SEC;
    gyro_z = (GyZ - baseGyZ) / GYROXYZ_TO_DEGREES_PER_SEC;
    //자이로 센서의 값을 각속도로 매핑
}
void calcFilteredYPR()
{
    const float ALPHA = 0.96;
    float tmp_angle_x, tmp_angle_y, tmp_angle_z;
    tmp_angle_x = filtered_angle_x + gyro_x * dt; //각속도에서 각도로 변환
    tmp_angle_y = filtered_angle_y + gyro_y * dt;
    tmp_angle_z = filtered_angle_z + gyro_z * dt;
    filtered_angle_x = ALPHA * tmp_angle_x + (1.0 - ALPHA) * accel_angle_x;
    filtered_angle_y = ALPHA * tmp_angle_y + (1.0 - ALPHA) * accel_angle_y;
    filtered_angle_z = tmp_angle_z;
}
int init_server() {
    int port = 3, result, client, bytes_read, bytes_sent;
    struct sockaddr_rc loc_addr = { 0 }, rem_addr = { 0 };
    char buffer[1024] = { 0 };
    socklen_t opt = sizeof(rem_addr);

    // local bluetooth adapter
    loc_addr.rc_family = AF_BLUETOOTH;
    loc_addr.rc_bdaddr = *BDADDR_ANY;
    loc_addr.rc_channel = (uint8_t) port;

    // register service
    sdp_session_t *session = register_service(port);
    

    // allocate socket
    sock = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
    int reuse = 1;
    setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &reuse, sizeof(reuse));
    printf("socket() returned %d\n", sock);

    
    // bind socket to port 3 of the first available
    result = bind(sock, (struct sockaddr *)&loc_addr, sizeof(loc_addr));
    printf("bind() on channel %d returned %d\n", port, result);

    // put socket into listening mode
    result = listen(sock, 1);
    printf("listen() returned %d\n", result);

    //sdpRegisterL2cap(port);

    // accept one connection
    printf("calling accept()\n");
    client = accept(sock, (struct sockaddr *)&rem_addr, &opt);
    printf("accept() returned %d\n", client);

    ba2str(&rem_addr.rc_bdaddr, buffer);
    fprintf(stderr, "accepted connection from %s\n", buffer);
    memset(buffer, 0, sizeof(buffer));

    return client;
}
sdp_session_t *register_service(uint8_t rfcomm_channel) {
    uint32_t svc_uuid_int[] = { 0x01110000, 0x00100000, 0x80000080, 0xFB349B5F };
    const char *service_name = "Armatus Bluetooth server";
    const char *svc_dsc = "A HERMIT server that interfaces with the Armatus Android app";
    const char *service_prov = "Armatus";

    uuid_t root_uuid, l2cap_uuid, rfcomm_uuid, svc_uuid,
            svc_class_uuid;
    sdp_list_t *l2cap_list = 0,
            *rfcomm_list = 0,
            *root_list = 0,
            *proto_list = 0,
            *access_proto_list = 0,
            *svc_class_list = 0,
            *profile_list = 0;
    sdp_data_t *channel = 0;
    sdp_profile_desc_t profile;
    sdp_record_t record = { 0 };
    sdp_session_t *session = 0;

    // set the general service ID
    sdp_uuid128_create(&svc_uuid, &svc_uuid_int);
    sdp_set_service_id(&record, svc_uuid);

    char str[256] = "";
    sdp_uuid2strn(&svc_uuid, str, 256);
    printf("Registering UUID %s\n", str);

    // set the service class
    sdp_uuid16_create(&svc_class_uuid, SERIAL_PORT_SVCLASS_ID);
    svc_class_list = sdp_list_append(0, &svc_class_uuid);
    sdp_set_service_classes(&record, svc_class_list);

    // set the Bluetooth profile information
    sdp_uuid16_create(&profile.uuid, SERIAL_PORT_PROFILE_ID);
    profile.version = 0x0100;
    profile_list = sdp_list_append(0, &profile);
    sdp_set_profile_descs(&record, profile_list);

    // make the service record publicly browsable
    sdp_uuid16_create(&root_uuid, PUBLIC_BROWSE_GROUP);
    root_list = sdp_list_append(0, &root_uuid);
    sdp_set_browse_groups(&record, root_list);

    // set l2cap information
    sdp_uuid16_create(&l2cap_uuid, L2CAP_UUID);
    l2cap_list = sdp_list_append(0, &l2cap_uuid);
    proto_list = sdp_list_append(0, l2cap_list);

    // register the RFCOMM channel for RFCOMM sockets
    sdp_uuid16_create(&rfcomm_uuid, RFCOMM_UUID);
    channel = sdp_data_alloc(SDP_UINT8, &rfcomm_channel);
    rfcomm_list = sdp_list_append(0, &rfcomm_uuid);
    sdp_list_append(rfcomm_list, channel);
    sdp_list_append(proto_list, rfcomm_list);

    access_proto_list = sdp_list_append(0, proto_list);
    sdp_set_access_protos(&record, access_proto_list);

    // set the name, provider, and description
    sdp_set_info_attr(&record, service_name, service_prov, svc_dsc);

    // connect to the local SDP server, register the service record,
    // and disconnect
    session = sdp_connect(BDADDR_ANY, BDADDR_LOCAL, SDP_RETRY_IF_BUSY);
    sdp_record_register(session, &record, 0);

    // cleanup
    sdp_data_free(channel);
    sdp_list_free(l2cap_list, 0);
    sdp_list_free(rfcomm_list, 0);
    sdp_list_free(root_list, 0);
    sdp_list_free(access_proto_list, 0);
    sdp_list_free(svc_class_list, 0);
    sdp_list_free(profile_list, 0);

    return session;
}