package com.mytask.abn.model;

import java.util.Objects;

public class ReportOutputData {


    private String Client_Information;
    private String Product_Information;
    private String Total_Transaction_Amount;

    public ReportOutputData(String client_Information, String product_Information, String total_Transaction_Amount) {
        Client_Information = client_Information;
        Product_Information = product_Information;
        Total_Transaction_Amount = total_Transaction_Amount;
    }

    public String getClient_Information() {
        return Client_Information;
    }

    public void setClient_Information(String client_Information) {
        Client_Information = client_Information;
    }

    public String getProduct_Information() {
        return Product_Information;
    }

    public void setProduct_Information(String product_Information) {
        Product_Information = product_Information;
    }

    public String getTotal_Transaction_Amount() {
        return Total_Transaction_Amount;
    }

    public void setTotal_Transaction_Amount(String total_Transaction_Amount) {
        Total_Transaction_Amount = total_Transaction_Amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportOutputData that = (ReportOutputData) o;
        return Objects.equals(getClient_Information(), that.getClient_Information()) && Objects.equals(getProduct_Information(), that.getProduct_Information()) && Objects.equals(getTotal_Transaction_Amount(), that.getTotal_Transaction_Amount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClient_Information(), getProduct_Information(), getTotal_Transaction_Amount());
    }
}
