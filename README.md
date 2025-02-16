# Evenue - Personalized Event Booking Platform

Evenue is a comprehensive event management and booking platform that simplifies event discovery, 
ticket booking, and personalized recommendations. The platform integrates a chatbot on WhatsApp using the Meta API, 
allowing users to interact and book events seamlessly.

Features

    🔥 Event Discovery & Personalized Recommendations
    🎟️ Ticket Booking & Confirmation
    📱 WhatsApp Chatbot Integration (via Meta API)
    🔐 User Authentication & Authorization
    📊 Machine Learning-Based Event Suggestions
    💬 Social Features – Users can post about events, like, and follow friends
    🎯 Admin Dashboard – Manage events, tickets, and users

## Technologies Used

- **Backend:** Java Spring Boot
- **Frontend:** HTML, CSS, Thymeleaf
- **Database:** SQLite
- **AI/ML:** Python Flask https://github.com/olawale021/event_recommender_system_api.git
- **Chatbot Integration:** Dialogflow & Meta API for WhatsApp
- **Build Tool:** Maven

- Project Structure

Evenue follows a well-structured MVC (Model-View-Controller) architecture.
1️⃣ Models (Entity Classes)

The models represent database entities, including:

    UserModel.java – Handles user authentication and profile details
    EventModel.java – Stores event details
    TicketModel.java – Manages ticket bookings
    TicketTypeModel.java – Defines ticket types (VIP, General, etc.)
    UserActivityModel.java – Tracks user interactions for recommendations
    etc

2️⃣ Controllers (REST APIs)

The controllers handle user interactions and API requests:

    DialogflowWebhookController.java – Manages chatbot interactions with Dialogflow
    WhatsAppWebhookController.java – Handles WhatsApp messages via Meta API
    EventController.java – Provides event-related endpoints
    TicketController.java – Manages ticket purchases and confirmations
    UserController.java – Handles user authentication and profile management


3️⃣ Services (Business Logic)

The services contain the main logic for handling data:

    UserService.java – Manages user authentication and profile retrieval
    EventService.java – Fetches event details and recommendations
    TicketService.java – Handles ticket booking and verification
    RecommendationService.java – Implements AI-based event recommendations

4️⃣ Repositories (Database Access)

The repositories interact with the database using Spring Data JPA:

    UserRepository.java – Handles user queries
    EventRepository.java – Fetches events from the database
    TicketRepository.java – Stores ticket bookings

## Installation & Setup

Prerequisites

    Java 17+
    SQLite
    Spring Boot

1. **Clone the repository:**

   git clone https://github.com/olawale021/evenue_main.git
   cd evenue_main
  

2. **Build the project:**
   
   mvn clean install
   

3. **Run the application:**
   
   mvn spring-boot:run
   

4. **Access the application:**
   - Open `http://localhost:8080` in your browser.

## WhatsApp Chatbot Integration

The chatbot is integrated using the Meta API, allowing users to interact via WhatsApp. Users can send messages to:
- Book tickets for events
- Get personalized event recommendations
- Receive booking confirmations

### Example Interaction:

User: Hi
Chatbot: Welcome! I can help you book tickets for your favorite events. What would you like to do?
User: I want to book a ticket for Night Life Party.
Chatbot: Sure! How many tickets do you need?


## Database

The application uses SQLite for data storage. The default database file is `database.db`. To reset the database, delete the file and restart the application.

## Contributing

Contributions are welcome! Follow these steps to contribute:
1. Fork the repository.
2. Create a new branch: `git checkout -b feature-name`
3. Make your changes and commit: `git commit -m "Add new feature"`
4. Push the branch: `git push origin feature-name`
5. Open a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

For any inquiries or suggestions, reach out via:
- **LinkedIn**: 

---
**Evenue** - Making Event Booking Smarter & More Personalized 🚀
