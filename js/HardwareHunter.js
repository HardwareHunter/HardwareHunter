let map;
let marker;
let polylines = [];
let userMarker;
let cart = [];
let markers = [];
let currentMarkerIndex = 0;
var sites = [];

function initMap() {
    map = L.map("map", {
        zoomControl: false, //prevent automatic zoom control
    }).setView([40.7128, -74.006], 13);

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {}).addTo(
        map
    );

    L.control
        .zoom({
            position: "bottomleft",
        })
        .addTo(map);
}

document.addEventListener("DOMContentLoaded", () => {
    const fetchSpecsButton = document.getElementById("fetch-specs");
    const gameInfo = document.getElementById("game-info");
    const steamInstructions = document.getElementById("steam-instructions");

    fetchSpecsButton.addEventListener("click", function () {
        const steamUrl = document.getElementById("steam-url").value;

        //Display the loading screen
        steamInstructions.style.display = "none";

        fetch("http://localhost:8000/fetch-specs", {
            method: "POST",
            headers: {
                "Content-Type": "text/plain",
            },
            body: steamUrl,
        })
            .then((response) => {
                if (!response.ok)
                    throw new Error("Network response was not ok.");
                return response.json();
            })
            .then((data) => {
                const gameData = data?.data;

                if (gameData) {
                    document.getElementById("game-name").innerText =
                        gameData.name || "Game Name Not Found";
                    document.getElementById("game-cover").src =
                        gameData.cover || "";
                    document.getElementById("min-specs").innerHTML =
                        gameData.minimum || "No minimum specs found.";
                    document.getElementById("rec-specs").innerHTML =
                        gameData.recommended || "No recommended specs found.";
                }

                //Show game info and hide loading screen
                gameInfo.style.display = "block";
                hideLoadingOverlay();
            })
            .catch((error) => {
                console.error("Error:", error);
                hideLoadingOverlay();
                alert(
                    "An error occurred while fetching game specs. Please try again."
                );
            });
    });
});
document.addEventListener("DOMContentLoaded", () => {
    initMap();

    const searchInput = document.getElementById("location-search");
    const searchButton = document.getElementById("search-button");

    //Hide Store Information heading initially on page load
    document.querySelector("#info-container h1").style.display = "none";

    //Show instructions initially on page load
    document.getElementById("instructions").style.display = "block";

    searchButton.addEventListener("click", () => {
        const query = searchInput.value.trim();
        if (query) {
            searchLocation(query);
        }
    });

    searchInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
            const query = searchInput.value.trim();
            if (query) {
                searchLocation(query);
            }
        }
    });

    const fetchSpecsButton = document.getElementById("fetch-specs");
    const steamInstructions = document.getElementById("steam-instructions");
    const gameInfo = document.getElementById("game-info");

    //Hide game info initially on page load
    gameInfo.style.display = "none";

    fetchSpecsButton.addEventListener("click", () => {
        steamInstructions.style.display = "none";
        showLoadingOverlay();
    });
});
document.querySelector(".right-button").addEventListener("click", () => {
    currentMarkerIndex = (currentMarkerIndex + 1) % markers.length;//Loop back to first store if at the end
    selectMarker(currentMarkerIndex);
});

document.querySelector(".left-button").addEventListener("click", () => {
    currentMarkerIndex =
        (currentMarkerIndex - 1 + markers.length) % markers.length; //Loop to last store if at the beginning
    selectMarker(currentMarkerIndex);
});

document.querySelector(".close-button").addEventListener("click", () => {
    const modal = document.getElementById("video-modal");
    modal.style.display = "none";

    document.getElementById("youtube-embed").src = "";
});

window.addEventListener("click", (event) => {
    const modal = document.getElementById("video-modal");
    if (event.target === modal) {
        modal.style.display = "none";

        document.getElementById("youtube-embed").src = "";
    }
});

document.querySelector(".cart-button").addEventListener("click", showCart);

function showLoadingOverlay() {
    document.getElementById("loading-overlay").style.display = "flex";
}

function hideLoadingOverlay() {
    document.getElementById("loading-overlay").style.display = "none";
}

function showLoadingOverlay() {
    document.getElementById("loading-overlay").style.display = "flex";
}

function hideLoadingOverlay() {
    document.getElementById("loading-overlay").style.display = "none";
}

function searchLocation(query) {
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
        query
    )}`;

    fetch(url)
        .then((response) => response.json())
        .then((data) => {
            if (data.length > 0) {
                const { lat, lon } = data[0];
                addUserMarker(lat, lon);
                map.setView([lat, lon], map.getZoom(), { animate: true });

                console.log("Searching stores for location:", lat, lon);
                searchStores(lat, lon);
            } else {
                alert("Location not found");
            }
        })
        .catch((error) => {
            console.error("Error:", error);
            alert("An error occurred while searching for the location");
        });
}

function searchStores(lat, lon) {
    const address = document.getElementById("location-search").value;
    const searchQuery = document.getElementById("PS-text").value;
    const searchButton = document.getElementById("search-button");
    const mapElement = document.getElementById("map");
    const loadingOverlay = document.getElementById("loading-overlay");
    const loadingText = document.getElementById("loading-text");


    searchButton.disabled = true;
    mapElement.classList.add("blur");
    loadingOverlay.style.display = "flex";

    //loading screen info
    const steps = [
        "Initializing search...",
        "Connecting to URLs...",
        "Scraping product information...",
        "Analyzing prices and availability...",
        "Generating map markers...",
        "Preparing results...",
    ];

    let stepIndex = 0;
    loadingText.textContent = "";

    function typeWriter() {
        if (stepIndex < steps.length) {
            loadingText.textContent += steps[stepIndex] + "\n";
            stepIndex++;
            setTimeout(typeWriter, 1000);
        }
    }

    typeWriter();

    fetch("http://localhost:8000/search-stores", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            zipCode: address,
            searchQuery,
            latitude: lat,
            longitude: lon,
        }),
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("Network response was not ok");
            }
            return response.json();
        })
        .then((data) => {
            displayStoresOnMap(data);
        })
        .catch((error) => {
            console.error("Error:", error);
            alert(
                "An error occurred while fetching store data. Please try again."
            );
        })
        .finally(() => {

            searchButton.disabled = false;
            mapElement.classList.remove("blur");
            loadingOverlay.style.display = "none";
        });
}

function showLoadingOverlay() {
    const loadingOverlay = document.getElementById("loading-overlay");
    const loadingText = document.getElementById("loading-text");
    loadingOverlay.style.display = "flex";

    const steps = [
        "Initializing search...",
        "Connecting to URLs...",
        "Scraping product information...",
        "Analyzing prices and availability...",
        "Generating map markers...",
        "Preparing results...",
    ];

    let i = 0;
    loadingText.textContent = "";

    function typeWriter() {
        if (i < steps.length) {
            loadingText.textContent += steps[i] + "\n";
            i++;
            setTimeout(typeWriter, 1000);
        }
    }

    typeWriter();
}

function initMap() {
    map = L.map("map", {
        zoomControl: false,
    }).setView([40.7128, -74.006], 13);

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {}).addTo(
        map
    );

    L.control
        .zoom({
            position: "bottomleft",
        })
        .addTo(map);


    getUserLocation();
}
document
    .getElementById("get-location-btn")
    .addEventListener("click", getUserLocation);

function getUserLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const lat = position.coords.latitude;
                const lon = position.coords.longitude;


                addUserMarker(lat, lon);
                map.setView([lat, lon], 13);


                reverseGeocode(lat, lon);
            },
            () => {
                alert("Unable to retrieve your location.");
            }
        );
    } else {
        alert("Geolocation is not supported by this browser.");
    }
}

function reverseGeocode(lat, lon) {
    const url = `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`;

    fetch(url)
        .then((response) => response.json())
        .then((data) => {
            if (data && data.display_name) {

                const locationSearchInput = document.getElementById(
                    "location-search"
                );
                locationSearchInput.value = data.display_name;
            } else {
                console.error("No address found for the location.");
            }
        })
        .catch((error) => {
            console.error("Error during reverse geocoding:", error);
        });
}

function addUserMarker(lat, lon) {
    if (userMarker) {
        map.removeLayer(userMarker);
    }

    userMarker = L.marker([lat, lon], {
        icon: L.divIcon({
            className: "custom-div-icon",
            html: createUserMarkerHTML(),
            iconSize: [30, 30],
            iconAnchor: [15, 30],
        }),
    }).addTo(map);
}

function showDropdown(dropdownId) {
    document.getElementById(dropdownId).style.display = "block";
}

function hideDropdown(dropdownId) {
    document.getElementById(dropdownId).style.display = "none";
}

function populateSearch(itemName) {
    document.getElementById("PS-text").value = itemName;
}

function createUserMarkerHTML() {
    return `<div style="
        position: relative;
        width: 30px;
        height: 30px;
        background-color: #efad44;
        border-radius: 50%;
        border: 2px solid #ecf0f1;
    "></div>`; //user marker styling
}

function selectMarker(index) {
    const marker = markers[index];
    if (marker) {
        marker.fire("click");
        const latlng = marker.getLatLng();
        map.setView(latlng, map.getZoom(), { animate: true });
    }
}

function displayStoresOnMap(data) {
    map.eachLayer((layer) => {
        if (layer instanceof L.Marker && layer !== userMarker) {
            map.removeLayer(layer);
        }
    });
    clearPolylines();
    markers = []; //Reset markers array
    const infoDisplay = document.getElementById("info-display");
    infoDisplay.innerHTML = "";

    const userLat = userMarker.getLatLng().lat;
    const userLon = userMarker.getLatLng().lng;

    if (Object.values(data).length === 0) {
        alert("No stores found.");
        map.setView([userLat, userLon], 13);
        return;
    }

    Object.values(data).forEach((store) => {
        const marker = L.marker([store.latitude, store.longitude]).addTo(map);
        markers.push(marker); //Add marker to the markers array
        const latlngs = [
            [userLat, userLon],
            [store.latitude, store.longitude],
        ];
        const polyline = L.polyline(latlngs, {
            color: "blue",
            dashArray: "5, 5",
        }).addTo(map);
        polylines.push(polyline);

        marker.on("click", () => {
            infoDisplay.innerHTML = `
                <div class="store-header">
                    <img src="https://upload.wikimedia.org/wikipedia/commons/f/f5/Best_Buy_Logo.svg" alt="Best Buy Logo" class="best-buy-logo">
                    <span>Location: <strong>${store.storeName}</strong></span>
                </div>
                <div class="products-list">
                    ${store.products
                .map((product) => {
                    if (!product.name || !product.currentPrice) {
                        return ""; 
                    }
                    return `
                            <div class="product-item" data-price="${parseFloat(
                        product.currentPrice.replace("$", "")
                    )}">
                                <input type="checkbox" id="product-${store.storeName.replace(
                        /\s+/g,
                        "-"
                    )}-${product.name.replace(/\s+/g, "-")}" 
                                    onchange="updateCart('${
                        store.storeName
                    }', '${product.name}', '${
                        product.currentPrice
                    }', this.checked)">
                                <label for="product-${store.storeName.replace(
                        /\s+/g,
                        "-"
                    )}-${product.name.replace(/\s+/g, "-")}">${
                        product.name
                    }</label>
                                <div style="font-weight: bold; font-size: 16px; margin-top: 5px;">${
                        product.currentPrice
                    }</div>
                                <button class="youtube-button" onclick="openYouTubeReview('${
                        product.name
                    }')" 
                                    style="background-color: #7c3434; color: white; border: none; padding: 6px 10px; border-radius: 5px; cursor: pointer; font-size: 14px;">
                                    <img src="https://img.icons8.com/color/48/000000/youtube-play.png" alt="YouTube" style="width: 18px; vertical-align: middle;"> Watch Reviews
                                </button>
                            </div>
                        `;
                })
                .join("")}
                </div>`;

            map.setView([store.latitude, store.longitude], map.getZoom(), {
                animate: true,
            });
        });
    });

    map.setView([userLat, userLon], 13);
}

function openYouTubeReview(productName) {

    const sanitizedProductName = productName
        .trim()
        .replace(/[^a-zA-Z0-9\s]+/g, "")
        .replace(/\s+/g, " ");

    const bingUrl = `https://www.bing.com/videos/search?q=${sanitizedProductName}&qs=n&form=QBVR&sp=-1&lq=0&pq=${sanitizedProductName}&sc=10-25&sk=&ghsh=0&ghacc=0&ghpl=`;

    const iframe = document.getElementById("youtube-embed");
    iframe.src = bingUrl;

    const modal = document.getElementById("video-modal");
    modal.style.display = "block";
}

function updateCart(storeName, productName, price, isChecked) {
    const sanitizedProductName = productName.replace(/['"]/g, "");
    if (isChecked) {
        cart.push({
            store: storeName,
            product: sanitizedProductName,
            price: price,
        });
    } else {
        cart = cart.filter(
            (item) =>
                !(
                    item.store === storeName &&
                    item.product === sanitizedProductName
                )
        );
    }
    refreshCartDisplay();
}

function refreshCartDisplay() {
    const cartButton = document.querySelector(".cart-button");
    cartButton.textContent = `Cart (${cart.length})`;

    if (document.getElementById("cart-window")) {
        showCart();
    }
}

function showCart() {
    const cartWindow = document.createElement("div");
    cartWindow.id = "cart-window";
    cartWindow.classList.add("cart-window");

    let cartContent = "<h2>Your Cart</h2>";
    const groupedCart = groupCartByStore(cart);

    if (cart.length === 0) {
        cartContent += "<p>Your cart is empty.</p>";
    } else {
        for (const [store, items] of Object.entries(groupedCart)) {
            cartContent += `<h3>${store}</h3>`;
            items.forEach((item, index) => {
                cartContent += `
                    <div class="cart-item">
                        <p>${item.product} - ${item.price}</p>
                        <button class="remove-item" onclick="removeFromCart('${store}', '${item.product}', ${index})">x</button>
                    </div>`;
            });
        }
    }

    cartContent += `
        <div class="cart-actions">
            <button onclick="closeCart()">Close</button>
            <button onclick="generateQRCode()">Generate QR Code</button>
        </div>
        <div id="qr-code-container" style="display: none;">
            <canvas id="qr-code"></canvas>
        </div>
    `;
    cartWindow.innerHTML = cartContent;

    const existingCartWindow = document.getElementById("cart-window");
    if (existingCartWindow) {
        existingCartWindow.innerHTML = cartContent;
    } else {
        const overlay = document.createElement("div");
        overlay.id = "cart-overlay";
        overlay.classList.add("cart-overlay");
        document.body.appendChild(overlay);
        document.body.appendChild(cartWindow);
    }
}

function generateQRCode() {
    const cartContent = cart
        .map((item) => `${item.product} - ${item.price} (${item.store})`)
        .join("\n");

    const qr = new QRious({
        element: document.getElementById("qr-code"),
        value: cartContent,
        size: 250,
    });

    document.getElementById("qr-code-container").style.display = "block";
}

function removeFromCart(store, product, index) {
    const sanitizedProductName = product.replace(/['"]/g, "");
    cart = cart.filter(
        (item, i) =>
            !(
                item.store === store &&
                item.product === sanitizedProductName &&
                i === index
            )
    );
    showCart();
}

function closeCart() {
    document.getElementById("cart-window").remove();
    document.getElementById("cart-overlay").remove();
}

function groupCartByStore(cart) {
    return cart.reduce((acc, item) => {
        if (!acc[item.store]) {
            acc[item.store] = [];
        }
        acc[item.store].push(item);
        return acc;
    }, {});
}

function clearPolylines() {
    polylines.forEach((polyline) => map.removeLayer(polyline));
    polylines = []; //Reset the array
}
