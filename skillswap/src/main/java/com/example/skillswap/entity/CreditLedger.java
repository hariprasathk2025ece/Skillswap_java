package com.example.skillswap.entity;

import jakarta.persistence.*;

@Entity
public class CreditLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which member this transaction belongs to
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private int amount;

    // Type: CREDIT or DEBIT
    private String type;

    private String description;

    // Default constructor required by JPA
    public CreditLedger() {}

    public CreditLedger(Member member, int amount, String type, String description) {
        this.member = member;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
