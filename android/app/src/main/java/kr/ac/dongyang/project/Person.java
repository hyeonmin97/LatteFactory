package kr.ac.dongyang.project;

public class Person {
    String name;
    String number;
    int gender_ID;

    public Person(String name, int gender_ID, String number) {
        this.name = name;
        this.gender_ID=gender_ID;
        this.number = number;
    }

    public int getGender_ID() {
        return gender_ID;
    }

    public void setGender_ID(int gender_ID) {
        this.gender_ID = gender_ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

}