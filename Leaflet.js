// Create the Leaflet map centered over Houston
const map = L.map('map').setView([29.76, -95.37], 13);

// Add the base map tiles
L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map);

// Harris county shape on the map overlay. Will add more stuff to make it more definite. Rough shape.
const polygon = L.polygon([
    [30.156630024414486, -95.95724219717924], //FieldsStore
    [29.791250, -95.821839], //Katy
    [29.578752, -95.423540], //ShadowCreekRanch
    [29.551440, -95.02469], // Galveston
    [29.678357, -94.932139], // Baytown
    [30.162513, -95.097959] // Plum Grove
], {
    color: 'darkgreen',
    fillColor: '#66bb6a',
    fillOpacity: 0.15
}).addTo(map);

// This layer is cleared and redrawn whenever new data comes in
const dynamicLayer = L.layerGroup().addTo(map);

//common ground closest to midpoint
const redIcon = new L.Icon({
    iconUrl: 'https://maps.google.com/mapfiles/ms/icons/red-dot.png',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32]
});

//user location
const blueIcon = new L.Icon({
    iconUrl: 'https://maps.google.com/mapfiles/ms/icons/blue-dot.png',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32]
});

//other common grounds
const greenIcon = new L.Icon({
    iconUrl: 'https://maps.google.com/mapfiles/ms/icons/green-dot.png',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32]
});

// Geocoder for converting user addresses into coordinates
const geocoder = L.esri.Geocoding.geocodeService();
function geocodeAddress(address) {
    return new Promise((resolve, reject) => {
        geocoder.geocode().text(address).run((error, result) => {
            if (error || !result || !result.results || result.results.length === 0) {
                reject(error || new Error(`No geocode result for: ${address}`));
                return;
            }

            const first = result.results[0];
            resolve({
                lat: first.latlng.lat,
                lon: first.latlng.lng
            });
        });
    });
}

// Calculate midpoint between two coordinates
function midpoint(a, b) {
    return {
        lat: (a.lat + b.lat) / 2,
        lon: (a.lon + b.lon) / 2
    };
}

// Draw the map based on backend data
async function loadAndRender() {
    const response = await fetch('/api/data');
    const data = await response.json();

    // Remove old markers and lines before redrawing
    dynamicLayer.clearLayers();

    // Geocode both user addresses from the DB
    const user1Coords = await geocodeAddress(data.user1.address);
    const user2Coords = await geocodeAddress(data.user2.address);

    // Compute the midpoint between the two user locations
    const mid = midpoint(user1Coords, user2Coords);

    // user location marker BLUE
    L.marker([user1Coords.lat, user1Coords.lon], { icon: blueIcon })
        .addTo(dynamicLayer)
        .bindPopup(`<b>${data.user1.name}</b><br>${data.user1.address}`);

    L.marker([user2Coords.lat, user2Coords.lon], { icon: greenIcon })
        .addTo(dynamicLayer)
        .bindPopup(`<b>${data.user2.name}</b><br>${data.user2.address}`);

    // Add midpoint marker
    L.marker([mid.lat, mid.lon], {
        icon: L.divIcon({
            className: 'midpoint-marker',
            html: '●',
            iconSize: [20, 20],
            iconAnchor: [10, 10]
        })
    }).addTo(dynamicLayer).bindPopup('Midpoint');

    // pull all Common Ground locations from the DB
    for (const cg of data.allCommonGrounds) {
        const marker = L.marker([cg.lat, cg.lon], {
            icon: redIcon
        }).addTo(dynamicLayer);

        let popupHtml = `<b>${cg.name}</b><br>${cg.address}<br>`;
        popupHtml += `<button onclick="selectCommonGround(${cg.id}, '${escapeQuotes(cg.address)}')">Request</button>`;
        popupHtml += `<br><button onclick="confirmCommonGround(${cg.id}, '${escapeQuotes(cg.address)}')">Confirm</button>`;

        marker.bindPopup(popupHtml);

        // Clicking a marker requests that meetup location and pushes back
        marker.on('click', () => {
            selectCommonGround(cg.id, cg.address);
        });
    }

    // Changes map zoom so users and meetup locations are visible
    const bounds = L.latLngBounds([
        [user1Coords.lat, user1Coords.lon],
        [user2Coords.lat, user2Coords.lon],
        [mid.lat, mid.lon],
        ...data.allCommonGrounds.map(cg => [cg.lat, cg.lon])
    ]);
    map.fitBounds(bounds, { padding: [40, 40] });
}

// Escape quotes so the address can safely be embedded into onclick handlers
function escapeQuotes(text) {
    return String(text).replace(/'/g, "\\'");
}

// Request a meetup location and store it in the DB through the .java
async function selectCommonGround(commonGroundId, meetupAddress) {
    await fetch('/api/select', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            transactionId: 1,
            commonGroundId: commonGroundId,
            meetupAddress: meetupAddress
        })
    });

    await loadAndRender();
}

// Confirm a meetup location and store it in the DB through the backend
async function confirmCommonGround(commonGroundId, meetupAddress) {
    await fetch('/api/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            transactionId: 1,
            commonGroundId: commonGroundId,
            meetupAddress: meetupAddress
        })
    });

    await loadAndRender();
}

// Expose functions so popup buttons can call them
window.selectCommonGround = selectCommonGround;
window.confirmCommonGround = confirmCommonGround;

// Initial render
loadAndRender();