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
  fetch('/leave-comment').then(response => response.json()).then((comments) => {
    const commentListElement = document.getElementById('comment-list');
    comments.forEach((comment) => {
      commentListElement.appendChild(createTaskElement(comment));
    })
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