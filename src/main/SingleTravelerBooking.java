
public class SingleTravelerBooking {

    public static BookedTrip bookSingleTraveler(Client client, Trip selectedTrip,
            boolean isFirstClass, TripCollection tripRepository) {
        // Validate client info
        if (client == null || !client.isValid()) {
            System.err.println("Error: Invalid client information");
            return null;
        }

        // Validate selected trip
        if (selectedTrip == null || selectedTrip.getSegments().isEmpty()) {
            System.err.println("Error: Invalid trip selected");
            return null;
        }

        // Validate collection
        if (tripRepository == null) {
            System.err.println("Error: Trip repository is null");
            return null;
        }

        try {
            // Get the connection from the first segment
            // Direct trips: only one segment
            // Multi-segment trips: create a reservation for the first connection
            Connection connection = selectedTrip.getSegments().get(0).getConnection();

            // Check if client already has a reservation for this connection
            if (tripRepository.hasReservationForConnection(client.getId(), connection)) {
                System.err.println("Error: Client already has a reservation for this connection");
                return null;
            }

            // Create new booked trip
            BookedTrip bookedTrip = new BookedTrip(selectedTrip, isFirstClass);

            // Create reservation for this client
            Reservation reservation = new Reservation(client, connection, isFirstClass);

            // Add reservation to the trip
            bookedTrip.addReservation(reservation);

            // Generate ticket for this reservation
            Ticket ticket = new Ticket(reservation);

            // Save trip to repository
            tripRepository.saveTrip(bookedTrip);

            // Print confirmation to console
            System.out.println("Booking was successful");
            System.out.println("Trip ID: " + bookedTrip.getTripId());
            System.out.println("Reservation ID: " + reservation.getReservationId());
            System.out.println("Ticket ID: " + ticket.getTicketId());
            System.out.println("Passenger: " + client.getFullName());
            System.out.println("Total Price: €" + String.format("%.2f", bookedTrip.getTotalPrice()));

            return bookedTrip;

        } catch (Exception e) {
            System.err.println("Error during booking: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static BookingResult bookSingleTravelerWithTicket(Client client, Trip selectedTrip,
            boolean isFirstClass, TripCollection tripRepository) {
        BookedTrip bookedTrip = bookSingleTraveler(client, selectedTrip, isFirstClass, tripRepository);

        if (bookedTrip != null && !bookedTrip.getReservations().isEmpty()) {
            Reservation reservation = bookedTrip.getReservations().get(0);
            Ticket ticket = new Ticket(reservation);
            return new BookingResult(bookedTrip, ticket);
        }

        return null;
    }

    public static class BookingResult {
        private BookedTrip bookedTrip;
        private Ticket ticket;

        public BookingResult(BookedTrip bookedTrip, Ticket ticket) {
            this.bookedTrip = bookedTrip;
            this.ticket = ticket;
        }

        public BookedTrip getBookedTrip() {
            return bookedTrip;
        }

        public Ticket getTicket() {
            return ticket;
        }
    }
}