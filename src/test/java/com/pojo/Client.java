package com.pojo;
import java.util.*;

public class Client {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Address address;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public static class Address {
        private String street;
        private String city;
        private String state;
        private String zipCode;

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }
    }

    public Map<String, Object> generatePojoMap() {

        Map<String, Object> pojoMap = new LinkedHashMap<>();
        pojoMap.put("id", getId());
        pojoMap.put("firstName", getFirstName());
        pojoMap.put("lastName", getLastName());
        pojoMap.put("email", getEmail());
        pojoMap.put("phone", getPhone());

        // Add address attributes to the map
        pojoMap.put("street", getAddress().getStreet());
        pojoMap.put("city", getAddress().getCity());
        pojoMap.put("state", getAddress().getState());
        pojoMap.put("zipCode", getAddress().getZipCode());

        return pojoMap;
    }
}
