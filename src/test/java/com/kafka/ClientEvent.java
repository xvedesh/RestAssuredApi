package com.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientEvent {
    private String eventType;
    private String entityType;
    private String clientId;
    private String timestamp;
    private ClientPayload payload;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ClientPayload getPayload() {
        return payload;
    }

    public void setPayload(ClientPayload payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "ClientEvent{" +
                "eventType='" + eventType + '\'' +
                ", entityType='" + entityType + '\'' +
                ", clientId='" + clientId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", payload=" + payload +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClientPayload {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private AddressPayload address;

        public String getId() {
            return id;
        }

        public void setId(String id) {
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

        public AddressPayload getAddress() {
            return address;
        }

        public void setAddress(AddressPayload address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return "ClientPayload{" +
                    "id='" + id + '\'' +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", address=" + address +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressPayload {
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

        @Override
        public String toString() {
            return "AddressPayload{" +
                    "street='" + street + '\'' +
                    ", city='" + city + '\'' +
                    ", state='" + state + '\'' +
                    ", zipCode='" + zipCode + '\'' +
                    '}';
        }
    }
}
