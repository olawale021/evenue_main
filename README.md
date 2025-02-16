# Evenue - Personalized Event Booking Platform

Evenue is a comprehensive event management and booking platform that simplifies event discovery, 
ticket booking, and personalized recommendations. The platform integrates a chatbot on WhatsApp using the Meta API, 
allowing users to interact and book events seamlessly.

## Features

- **Event Discovery:** Browse upcoming events with categories and filters.
- **Personalized Recommendations:** Event suggestions based on user preferences and interactions.
- **WhatsApp Chatbot Integration:** Book tickets and get event details via WhatsApp.
- **User Authentication:** Secure login and registration.
- **Event Management:** Organizers can create and manage events.
- **Ticket Booking System:** Seamless ticket reservation with real-time updates.

## Technologies Used

- **Backend:** Java Spring Boot
- **Frontend:** HTML, CSS, Thymeleaf
- **Database:** SQLite
- **AI/ML:** Python Flask https://github.com/olawale021/event_recommender_system_api.git
- **Chatbot Integration:** Dialogflow & Meta API for WhatsApp
- **Build Tool:** Maven

## Installation & Setup

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
