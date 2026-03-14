package presentation.controller;

public class FinanceRecord {
    private String type;      // "E" or "S"
    private String data;
    private String description;
    private String origin;
    private Double value;

    public FinanceRecord(String type, String data, String description, String origin, Double value) {
        this.type = type;
        this.data = data;
        this.description = description;
        this.origin = origin;
        this.value = value;
    }

    // Getters 
    public String getType() { return type; }
    public String getData() { return data; }
    public String getDescription() { return description; }
    public String getOrigin() { return origin; }
    public Double getValue() { return value; }
}