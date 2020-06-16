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
import com.google.sps.data.Marker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Returns inital marker data as a JSON array e.g. [{"lat": 36.698, "lng": 99.106, "title": "xx",
 * "Content": "yy"}]
 */
@WebServlet("/initial-marker")
public class InitialMarkerServlet extends HttpServlet {

  private List<Marker> markers;

  @Override
  public void init() {
    markers = new ArrayList<>();

    Scanner scanner =
        new Scanner(getServletContext().getResourceAsStream("/WEB-INF/marker-data.csv"));
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      // Parse CSV file with comma in field
      String[] cells = line.split(",(?=([^\"]|\"[^\"]*\")*$)");
      double lat = Double.parseDouble(cells[0]);
      double lng = Double.parseDouble(cells[1]);
      String title = cells[2];
      String content = cells[3];

      markers.add(new Marker(lat, lng, title, content));
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(markers);
    response.getWriter().println(json);
  }
}
