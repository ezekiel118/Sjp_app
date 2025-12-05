package com.example.sjp_app.data;

public class ClearanceItem {
    public String office;
    public boolean cleared;
    public String remarks;

    public ClearanceItem() {
        // Default constructor required for calls to DataSnapshot.getValue(ClearanceItem.class)
    }

    public ClearanceItem(String office, boolean cleared, String remarks) {
        this.office = office;
        this.cleared = cleared;
        this.remarks = remarks;
    }
}
