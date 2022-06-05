package com.oreilly.persistence.entities;


import org.springframework.data.annotation.Id;

public class ReactiveOfficer {
    @Id
    private Integer id;

    private Rank rank;

    private String firstName;
    private String lastName;

    public ReactiveOfficer() {}

    public ReactiveOfficer(Rank rank, String firstName, String lastName) {
        this.rank = rank;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public ReactiveOfficer(Integer id, Rank rank, String firstName, String lastName) {
        this.id = id;
        this.rank = rank;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String first) {
        this.firstName = first;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String last) {
        this.lastName = last;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", rank, firstName, lastName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReactiveOfficer)) return false;

        ReactiveOfficer officer = (ReactiveOfficer) o;

        if (!id.equals(officer.id)) return false;
        if (rank != officer.rank) return false;
        if (firstName != null ? !firstName.equals(officer.firstName) : officer.firstName != null) return false;
        return lastName.equals(officer.lastName);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + rank.hashCode();
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + lastName.hashCode();
        return result;
    }
}
