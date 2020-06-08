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

package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;

/** Servlet that returns some example content. */
@WebServlet("/leave-comment")
public class DataServlet extends HttpServlet {

	ArrayList<Comment> messages = new ArrayList<Comment>();
	int counter = -1;

	/** A comment in the comment section. */
	public final class Comment {
		private final long id;
		private final String text;
		private final long timestamp;

		public Comment(long id, String text, long timestamp) {
			this.id = id;
			this.text = text;
			this.timestamp = timestamp;
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		messages.clear();
		Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery results = datastore.prepare(query);

		// Get a limited number of comments
		// counter should already be set to the appropriate number through doPOST
		int countComment = 0;
		for (Entity entity : results.asIterable()) {
			if (countComment == counter) {
				break;
			}
			long id = entity.getKey().getId();
			String text = (String) entity.getProperty("text");
			long timestamp = (long) entity.getProperty("timestamp");

			Comment comment = new Comment(id, text, timestamp);
			messages.add(comment);
			++countComment;
		}

		// Convert the ArrayList to JSON
		Gson gson = new Gson();

		// Send the JSON as the response
		response.setContentType("application/json");
		response.getWriter().println(gson.toJson(messages));
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// If statement determines whether the POST request is
		// from limiting a certain number of comments to display
		// or from leaving a new comment
		if (request.getParameter("numComment") != null) {
			counter = Integer.parseInt(request.getParameter("numComment"));
		} else {
			String text = request.getParameter("comment");
			long timestamp = System.currentTimeMillis();

			Entity commentEntity = new Entity("Comment");
			commentEntity.setProperty("text", text);
			commentEntity.setProperty("timestamp", timestamp);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(commentEntity);
		}

		// Redirect back to the HTML page.
		response.sendRedirect("/index.html");
		}
}
