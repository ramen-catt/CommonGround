// map centered over Houston.
const map = L.map('map').setView([29.76, -95.37], 12);

// visible tiles
L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    // Show attribution required by OpenStreetMap.
    attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map);

// Harris county shape on the map overlay. Will add more stuff to make it more definite. Rough shape.
const polygon = L.polygon([
    [30.02998, -95.30423], //kenswick
    [30.05562, -95.32406], //carter park
    [30.08605, -95.37713], //pundt park
    [30.10316, -95.47261], //mossy oaks
    [30.11204, -95.49196], //5 oaks
    [30.16918, -95.54168], //Kyukendal rd by indian springs
    [30.13426, -95.59695], //northwoods estates
    [30.13515, -95.62305], //between spring creek hollow and north woods
    [30.12713, -95.62390], //spring creek hollow
    [30.11911, -95.64605], //tomball
    [30.156630, -95.957242], //FieldsStore
    [29.791250, -95.821839], //Katy
    [29.578752, -95.423540], //ShadowCreekRanch
    [29.59762, -95.33433], //brookside village
    [29.551440, -95.02469], // Galveston
    [29.55242, -95.02208], //kemah
    [29.66339, -95.00129], //LaPorte
    [29.68143, -94.97013], //morgan's point
    [29.678357, -94.932139], // Baytown
    [29.79985, -94.92478], // Pinehurst
    [29.97904, -94.99482], //spindletop
], {
    color: 'darkgreen',
    fillColor: '#66bb6a',
    fillOpacity: 0.2
}).addTo(map);

const dynamicLayer = L.layerGroup().addTo(map);

// Red icon for normal Common Ground locations.
const commonGroundIcon = new L.Icon({
    // Image used for the marker.
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
    // Marker icon width and height.
    iconSize: [25, 41],
    // Anchor point at the bottom center of the icon.
    iconAnchor: [12, 41],
    // Popup should appear slightly above the marker.
    popupAnchor: [1, -34],
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
    shadowSize: [41, 41]
});

//  Common Ground locations
const commonGrounds = [
    // Common Ground 1.
    { id: 1, name: 'HPD Headquarter', address: '1200 Travis St Houston TX', lat: 29.75568, lon: -95.36739 },
    { id: 2, name: 'HPD Magnolia Park', address: '7525 Sherman St Houston TX', lat: 29.73425, lon: -95.29035 },
    { id: 3, name: 'HPD Westbury', address: '5600 S Willow Dr 116 Houston TX', lat: 29.65195, lon: -95.47672 },
    { id: 4, name: 'HPD Northwest', address: '6000 Teague Rd Houston TX', lat: 29.85814, lon: -95.53938 },
    { id: 5, name: 'HPD Westside', address: '3203 Dairy Ashford Rd Houston TX', lat: 29.74083, lon: -95.60423 },
    { id: 6, name: 'HPD North', address: '9455 W Montgomery Rd Houston TX', lat: 29.88620, lon: -95.44561 },
    { id: 7, name: 'HPD Southeast', address: '8300 Mykawa Rd Houston TX', lat: 29.66628, lon: -95.32202 }
];

// puts all Common Ground markers on the map.
function renderCommonGrounds() {
    // Remove old markers before drawing new ones.
    dynamicLayer.clearLayers();

    // each common ground gets its own marker.
    for (const cg of commonGrounds) {

        const marker = L.marker([cg.lat, cg.lon], {
            icon: commonGroundIcon
        }).addTo(dynamicLayer);

        // Bind a popup with the location name and address.
        marker.bindPopup(`<b>${cg.name}</b><br>${cg.address}`);
    }
}

// API KEY SUPER IMPORTANT
const EsriKey = "AAPTadWWGuAAgVxflKQV3KUYdfw..ldf0mZlOZA3zmhAwX8LLjTnxTKRws0R0IBedC81BXLgWxkkORzfHX5AIeVu3rG1dy3y5INkOwiX8bZ_EZI1ageKEQSC7FdpGZfB0jO5Jv2yRKIrwzhxjXm9sI_-H9gzyQHT6rdxDeXhTlUBepgHzm1rmznYQfzdgJlhjT3ZU85enL9ONzGG8ICdXj9dXiz2yTdNU3lA5m2DdvG0Jop5vFsvW_5uTgbbLhodKpIzD1bER7BB1LnY.AT1_VTOxrQ0l"; // Paste the key from the website here

function geocodeAddress(address, callback) {
    // we pass the key inside the geocode() options object
    L.esri.Geocoding.geocode({
        apikey: EsriKey
    }).text(address).run(function (err, results, response) {
        if (results && results.results && results.results.length > 0) {
            callback(results.results[0].latlng);
        } else {
            console.warn("Geocoding failed for: " + address);
            if (err) console.error(err);
        }
    });
}

// change icon to ladybug for selected commonground
function highlightSelectedGround(selectedName) {
    dynamicLayer.eachLayer(function(layer) {
        if (layer.getPopup().getContent().includes(selectedName)) {

            layer.setIcon(new L.Icon({
                iconUrl: 'data:image/png;base64,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',
                iconSize: [40, 40]
            }));
            layer.openPopup();
        }
    });
}

// Render the markers immediately when the script loads.
renderCommonGrounds();

//js to look for which transaction we calculating for
async function processTransaction(transactionId) {
    // 1. Get addresses from DB
    const response = await fetch(`/get-meetup?id=${transactionId}`);
    const data = await response.text();
    const [buyerAddr, sellerAddr] = data.split("|");

    // 2. Geocode User A
    geocodeAddress(buyerAddr, function(buyerLatLng) {
        // 3. Geocode User B
        geocodeAddress(sellerAddr, function(sellerLatLng) {

            // 4. Send all 4 coordinates to Java
            const url = `/calculate-closest?id=${transactionId}` +
                `&latB=${buyerLatLng.lat}&lonB=${buyerLatLng.lng}` +
                `&latS=${sellerLatLng.lat}&lonS=${sellerLatLng.lng}`;

            fetch(url)
                .then(res => res.text())
                .then(closestHPDName => {
                    // 5. Success! Highlight the HPD spot on the map
                    console.log("Closest HPD is:", closestHPDName);
                    highlightSelectedGround(closestHPDName);
                });
        });
    });
}