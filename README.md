![Hardware Hunter Logo](https://github.com/HardwareHunter/HardwareHunter/blob/main/images/logo.png)

# Hardware Hunter

**Hardware Hunter** is a web application designed to optimize the shopping experience for PC builders and gamers. It combines mapping technology with real-time inventory tracking of PC components available at major retailers. This platform allows users to search for specific parts, filter by type, and view local stores with available stock on a user-friendly interactive map.

## Motivation

The project arose from the challenges faced by PC builders in quickly sourcing components needed for building or repairing their setups. Delivery times and shipping costs can often hinder the process, particularly for those on a budget. Hardware Hunter delivers a solution by consolidating inventory data across multiple retailers, thereby enabling users to find nearby stores with required components in stock efficiently. Ultimately, this application aims to save time and money while streamlining the process of purchasing PC hardware.

## Features
- **Real-Time Inventory Tracking**: Find out which local stores have the PC components you need in stock.
- **User-Friendly Map Interface**: The interactive map shows the locations of nearby retailers with available inventory.
- **Steam Game Specs Integration**: Users can enter a Steam game URL to retrieve minimum and recommended hardware specifications, helping choose compatible components.
- **Advanced Filter Options**: Search and filter components by category (CPU, GPU, RAM, etc.).
- **Shopping Cart Functionality**: Users can manage and keep track of selected components with a cart feature.
- **YouTube Reviews**: Access product review videos directly from the app.

## Tech Stack

- **Frontend**: 
  - HTML5
  - CSS3
  - JavaScript
  - Leaflet.js (for mapping)
  - jQuery (for DOM manipulation)
  - Font Awesome (for icons)

- **Backend**: 
  - Java (for web scraping and handling requests)
  - Jsoup (for web scraping)
  - JSON (for data interchange)

## Keywords
PC component mapping, Real-time inventory tracking, Interactive map, PC building, Hardware locator, Retailer integration

## How to Use Hardware Hunter
1. **Enter Your Location**: Input your location in the "Enter location..." field or use the "Get My Location" button.
2. **Search for Parts**: Type in the PC part you are looking for in the "Search for parts" field.
3. **View Results**: Click the "Search" button to discover local stores with the desired components.

## Setup Instructions
1. Clone this repository:
   ```bash
   git clone https://github.com/D34nTheB34n/HardwareHunter.git
   cd HardwareHunter
   ```
2. Navigate to the backend folder and install necessary libraries:
   ```bash
   cd backend/main/src
   # Ensure you have your environment ready (Java)
   ```
3. Run the backend server:
   ```bash
   java - HttpServer.java
   ```
4. Navigate to the frontend folder and open `index.html` in your browser.

