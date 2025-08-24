# Spincery - Complete Lifestyle Platform

A comprehensive online platform for food delivery, hotel room booking, dining table booking, and cloud kitchen services built with Spring Boot, Thymeleaf, and MySQL.

## 🚀 Features

### 🍕 Food Delivery
- Dynamic restaurant listing with search by location
- Restaurant owner dashboard with order management
- Real-time order tracking
- Menu management system
- Payment integration

### 🏨 Hotel Room Booking
- Hotel listing and room availability
- Room service management
- Booking management with check-in/check-out
- Hotel owner dashboard
- Payment processing

### 🍽️ Dining Table Booking
- Restaurant table reservation system
- Real-time table availability
- Special requests and customization
- Booking management for restaurants

### ☁️ Cloud Kitchen
- Subscription-based meal plans
- Daily meal delivery (breakfast, lunch, dinner)
- Dietary preference management
- Cloud kitchen owner dashboard

### 👤 User Management
- User registration and authentication
- Role-based access control
- User dashboard with order history
- Profile management

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.5.3
- **Database**: MySQL 8.0
- **Template Engine**: Thymeleaf
- **Security**: Spring Security
- **Build Tool**: Maven
- **Frontend**: HTML, CSS, JavaScript, Tailwind CSS
- **Icons**: Font Awesome

## 📋 Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

## 🚀 Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd spincery
```

### 2. Database Setup
1. Create a MySQL database named `spincery`
2. Update database credentials in `src/main/resources/application.properties`

### 3. Configure Application Properties
Update the following in `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/spincery?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password

# Mail Configuration (for email verification)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 4. Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### 5. Access the Application
- **Main Application**: http://localhost:8080
- **Login Page**: http://localhost:8080/login
- **Register Page**: http://localhost:8080/register
- **Dashboard**: http://localhost:8080/dashboard

## 📁 Project Structure

```
src/main/java/in/sp/main/
├── config/                 # Configuration classes
│   └── SecurityConfig.java
├── controller/             # Controller classes
│   ├── HomeController.java
│   └── RestaurantController.java
├── entity/                 # JPA entities
│   ├── User.java
│   ├── Restaurant.java
│   ├── MenuItem.java
│   ├── FoodOrder.java
│   ├── OrderItem.java
│   ├── Hotel.java
│   ├── Room.java
│   ├── RoomBooking.java
│   ├── TableBooking.java
│   ├── CloudKitchen.java
│   ├── MealPlan.java
│   └── CloudKitchenSubscription.java
├── repository/             # Repository interfaces
│   ├── UserRepository.java
│   ├── RestaurantRepository.java
│   ├── MenuItemRepository.java
│   └── FoodOrderRepository.java
├── service/                # Service interfaces and implementations
│   ├── UserService.java
│   ├── CustomUserDetailsService.java
│   └── impl/
│       └── UserServiceImpl.java
└── SpinceryApplication.java

src/main/resources/
├── templates/              # Thymeleaf templates
│   ├── index.html
│   ├── login.html
│   ├── register.html
│   ├── dashboard.html
│   └── restaurant/
│       └── dashboard.html
└── application.properties
```

## 🔐 Security Features

- **User Authentication**: Email/password-based login
- **Role-based Access Control**: Different roles for customers, restaurant owners, hotel owners, etc.
- **Password Encryption**: BCrypt password hashing
- **Email Verification**: Account verification via email
- **Session Management**: Secure session handling

## 🎨 UI/UX Features

- **Responsive Design**: Works on desktop, tablet, and mobile
- **Modern UI**: Clean and intuitive interface using Tailwind CSS
- **User Avatars**: Circular user initials display
- **Status Badges**: Color-coded status indicators
- **Interactive Elements**: Hover effects and transitions

## 📊 Dashboard Features

### User Dashboard
- Profile overview
- Order history with tracking
- Hotel bookings
- Cloud kitchen subscriptions
- Dining table reservations
- Notifications panel

### Restaurant Dashboard
- Order management (accept, reject, update status)
- Sales analytics
- Menu management
- Customer insights
- Real-time order updates

## 🔧 Configuration

### Database Configuration
The application uses MySQL with the following default settings:
- Database: `spincery`
- Username: `root`
- Password: `root`
- Port: `3306`

### Email Configuration
For email verification to work, configure SMTP settings:
- Gmail SMTP recommended
- App password required for Gmail
- TLS encryption enabled

## 🚀 Deployment

### Local Development
```bash
mvn spring-boot:run
```

### Production Deployment
1. Build the JAR file:
   ```bash
   mvn clean package
   ```

2. Run the JAR file:
   ```bash
   java -jar target/spincery-0.0.1-SNAPSHOT.jar
   ```

## 📝 API Endpoints

### Public Endpoints
- `GET /` - Home page
- `GET /login` - Login page
- `GET /register` - Registration page
- `GET /verify` - Email verification

### Protected Endpoints
- `GET /dashboard` - User dashboard
- `GET /restaurant/dashboard` - Restaurant dashboard
- `GET /hotel/dashboard` - Hotel dashboard
- `GET /cloud-kitchen/dashboard` - Cloud kitchen dashboard

## 🔄 Database Schema

The application creates the following tables automatically:
- `users` - User accounts and profiles
- `restaurants` - Restaurant information
- `menu_items` - Restaurant menu items
- `food_orders` - Food delivery orders
- `order_items` - Individual items in orders
- `hotels` - Hotel information
- `rooms` - Hotel room details
- `room_bookings` - Hotel room bookings
- `table_bookings` - Dining table reservations
- `cloud_kitchens` - Cloud kitchen information
- `meal_plans` - Cloud kitchen meal plans
- `cloud_kitchen_subscriptions` - User subscriptions

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project not licenced

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Contact the development team

## 🔮 Future Enhancements

- Real-time notifications using WebSocket
- Mobile app development
- Advanced analytics and reporting
- Payment gateway integration
- Multi-language support
- Advanced search and filtering
- Review and rating system
- Loyalty program implementation 
