// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random quote to the page.
 */
function addRandomQuote() {
  const quotes =
      ['I still don\'t know what it really means to grow up. However, \
      if I happen to meet you, one day in the future, by then, \
      I want to become someone you can be proud to know.', 
      'I probably just want to leave a trace of myself behind in this world.', 
      'It must really be a lonelier journey than anyone could imagine. \
      Cutting through absolute darkness, \
      encountering nothing but the occasional hydrogen atom. \
      Flying blindly into the abyss, \
      believing therein lie the answers to the mysteries of the universe.'];

  // Pick a random quote.
  const quote = quotes[Math.floor(Math.random() * quotes.length)];

  // Add it to the page.
  const quoteContainer = document.getElementById('quote-container');
  quoteContainer.innerText = quote;
}

function getComment() {
  determineLogin();
  fetch('/leave-comment').then(response => response.json()).then((comments) => {
    const commentListElement = document.getElementById('comment-list');
    comments.forEach((comment) => {
      commentListElement.appendChild(createTaskElement(comment));
    })
  }).catch(error => {
    console.error('There has been a problem with your operation:', error);
  });
}

function determineLogin() {
  fetch('/get-login-info').then(response => response.json()).then((isLoggedIn) => {
    console.log("Displaying login info")
    const element = document.getElementById('form');
    const textElement = document.createElement('span');
    const form = document.createElement('form');
    form.method='GET';
    form.action='/login';
    const button = document.createElement('button');
    if (!isLoggedIn) {
      textElement.innerText = "Please log in to leave comment";
      button.innerHTML = "Login";
    } else {
      textElement.innerText = "You're logged in!";
      button.innerHTML = "Logout";
    }

    form.appendChild(button);
    element.appendChild(textElement);
    element.appendChild(form);
  }).catch(error => {
    console.error('There has been a problem with your operation:', error);
  });
}

/** Create HTML elements */
function createTaskElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment';

  const textElement = document.createElement('span');
  textElement.innerText = comment.text;

  commentElement.appendChild(textElement);
  return commentElement;
}

/** Tells the server to delete all comments. */
function deleteAllComments() {
  fetch('/delete-data', {method: 'POST'})
    .then(() => fetch('/leave-comment')).then(() => location.reload())
    .catch(error => {
    console.error('There has been a problem with your operation:', error);
    });
}

let map;

/* Editable marker that displays when a user clicks in the map. */
let editMarker;

/** Creates a map that allows users to add markers. */
function createMap() {
  map = new google.maps.Map(
    document.getElementById('map'),
    {center: {lat: 34.995, lng: 135.785}, zoom: 1});

  // When the user clicks in the map, show a marker with a text box the user can
  // edit.
  map.addListener('click', (event) => {
    createMarkerForEdit(event.latLng.lat(), event.latLng.lng());
  });

  fetchInitialMarkers()
  fetchMarkers();
}

/** Fetches inital marker data from the server and displays it in a map. */
function fetchInitialMarkers() {
  fetch('/initial-marker').then(response => response.json()).then((markers) => {
    markers.forEach(marker => {
      createMarkerForDisplay(marker.lat, marker.lng, marker.title, marker.content)});
  });
}

/** Fetches markers from the backend and adds them to the map. */
function fetchMarkers() {
  fetch('/markers').then(response => response.json()).then((markers) => {
    markers.forEach(marker => {
      createMarkerForDisplay(marker.lat, marker.lng, marker.title, marker.content)});
  });
}

/** Creates a marker that shows a read-only info window when clicked. */
function createMarkerForDisplay(lat, lng, title, content) {
  const marker =
    new google.maps.Marker({position: {lat: lat, lng: lng}, map: map, title: title});

  const infoWindow = new google.maps.InfoWindow({content: content});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
    map.setZoom(10);
    map.setCenter(LatLng(marker.getLat(), marker.getLng()));
  });
  // When the user closes the editable info window, zooms out.
  google.maps.event.addListener(infoWindow, 'closeclick', () => {
    map.setZoom(1);
  });
}

/** Sends a marker to the backend for saving. */
function postMarker(lat, lng, title, content) {
  const params = new URLSearchParams();
  params.append('lat', lat);
  params.append('lng', lng);
  params.append('title', title);
  params.append('content', content);

  fetch('/markers', {method: 'POST', body: params});
}

/** Creates a marker that shows textboxes the user can edit. */
function createMarkerForEdit(lat, lng) {
  // If we're already showing an editable marker, then remove it.
  if (editMarker) {
    editMarker.setMap(null);
  }

  editMarker =
    new google.maps.Marker({position: {lat: lat, lng: lng}, map: map});

  const infoWindow =
    new google.maps.InfoWindow({content: buildInfoWindowInput(lat, lng)});

  // When the user closes the editable info window, remove the marker.
  google.maps.event.addListener(infoWindow, 'closeclick', () => {
    editMarker.setMap(null);
  });

  infoWindow.open(map, editMarker);
}

/**
 * Builds and returns HTML elements that show two editable textboxes and
 * a submit button.
 */
function buildInfoWindowInput(lat, lng) {
  const title = document.createElement('textarea');
  const content = document.createElement('textarea');
  const button = document.createElement('button');
  button.appendChild(document.createTextNode('Submit'));

  button.onclick = () => {
    postMarker(lat, lng, title.value, content.value);
    createMarkerForDisplay(lat, lng, title.value, content.value);
    editMarker.setMap(null);
  };

  const containerDiv = document.createElement('div');
  containerDiv.appendChild(document.createTextNode("Title:")); 
  containerDiv.appendChild(document.createElement('br'));
  containerDiv.appendChild(title);
  containerDiv.appendChild(document.createElement('br'));
  containerDiv.appendChild(document.createTextNode("Content:"));
  containerDiv.appendChild(document.createElement('br'));
  containerDiv.appendChild(content);
  containerDiv.appendChild(document.createElement('br'));
  containerDiv.appendChild(button);

  return containerDiv;
}
