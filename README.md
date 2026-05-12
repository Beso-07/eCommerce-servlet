# eCommerce Web Application

A full-featured eCommerce platform built with Java servlets and JSP, designed as a final year project for Advanced Programming course. This application provides a complete online shopping experience with user authentication, product management, and review system.

## Features

### For Customers
- **User Registration & Login** - Secure authentication with JWT tokens
- **Product Browsing** - View all available products with details
- **Product Reviews** - Read and write reviews for products
- **User Profile** - Manage personal information

### For Administrators
- **Admin Dashboard** - Complete control panel for store management
- **Product Management** - Add, edit, and remove products

## Architecture

This application follows MVC (Model-View-Controller) architecture:

### Models
- **User** - Handles user data and authentication
- **Product** - Manages product information
- **Review** - Handles product reviews and ratings

### Controllers
- **AuthServlet** - Manages user authentication (login, register, logout)
- **ProductServlet** - Handles product operations and display
- **AdminServlet** - Administrative functions
- **ProfileServlet** - User profile management
- **ReviewServlet** - Review system operations

### Views (JSP Pages)
- **index.jsp** - Home page with product listings
- **login.jsp** - User login form
- **register.jsp** - User registration form
- **product-details.jsp** - Individual product page
- **admin-dashboard.jsp** - Admin control panel
- **profile.jsp** - User profile management

## Technology Stack

### Backend
- **Java 17** - Core programming language
- **Jakarta Servlets** - Web framework for handling HTTP requests
- **JSP (JavaServer Pages)** - View technology for dynamic content
- **MySQL** - Primary database for storing users, products, and reviews
- **Redis** - Caching layer for improved performance
- **JWT (JSON Web Tokens)** - Secure authentication tokens

### Frontend
- **HTML5 & CSS3** - Modern web standards
- **JavaScript** - Client-side interactivity

### Build & Deployment
- **Maven** - Dependency management and build automation
- **Apache Tomcat** - Web server for deployment

## Project Structure

```
eCommerce/
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/
│   │   │   ├── controller/     # Servlet controllers
│   │   │   ├── dao/           # Data Access Objects
│   │   │   ├── exception/     # Custom exceptions
│   │   │   ├── filter/        # Security filters
│   │   │   ├── helper/        # Utility classes
│   │   │   ├── model/         # Data models
│   │   │   ├── service/       # Business logic
│   │   │   └── util/          # General utilities
│   │   ├── resources/
│   │   │   └── schema.sql     # Database schema
│   │   └── webapp/
│   │       ├── css/           # Stylesheets
│   │       ├── js/            # JavaScript files
│   │       ├── WEB-INF/
│   │       │   └── web.xml    # Deployment descriptor
│   │       └── *.jsp          # JSP pages
│   └── test/                  # Unit tests
├── pom.xml                    # Maven configuration
└── README.md
