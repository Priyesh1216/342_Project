/*
    Represents a client/traveler in the railway booking system.
    Each client has personal information including name, age, and ID.
    Used for creating reservations and booking trips.
*/
public class Client {
    private String firstName;
    private String lastName;
    private int age;
    private String id; // Generic ID (state-id or passport number)

    /**
     * Constructor to create a new Client
     * 
     * @param firstName The client's first name
     * @param lastName  The client's last name
     * @param age       The client's age
     * @param id        The client's identification number (state ID or passport)
     */
    public Client(String firstName, String lastName, int age, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.id = id;
    }

    // Getters
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Setters
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isValid() {
        // Check that the first name is not empty
        if (firstName == null || firstName.trim().isEmpty()) {
            return false;
        }
        // Check that the last name is not empty
        if (lastName == null || lastName.trim().isEmpty()) {
            return false;
        }
        // Check if age is between valid range (between 0 and 120)
        if (age <= 0 || age > 150) {
            return false;
        }
        // Check that the client ID is not empty
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean matchesCredentials(String lastName, String id) {
        return this.lastName.equalsIgnoreCase(lastName) && this.id.equals(id);
    }

    @Override
    public String toString() {
        return "Client: " + getFullName() + " (Age: " + age + ", ID: " + id + ")";
    }

    // Client objects are equal if they have the same ID
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Client client = (Client) obj;
        return id.equals(client.id);
    }
}