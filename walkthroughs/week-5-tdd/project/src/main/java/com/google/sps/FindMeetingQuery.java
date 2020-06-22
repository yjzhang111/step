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

package com.google.sps;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


/*  Find time slot(s) that all the required attendees are availble
    Include optional attendees if at least one time slot can be found
    Otherwise return the time slots that fit just the mandatory attendees.
*/
public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Initialize a time slot (the whole day)
    List<TimeRange> timeSlots = new ArrayList<>();
    timeSlots.add(TimeRange.fromStartDuration(0, 24 * 60));

    // Find time slots for required attendees
    timeSlots = queryForEvent(events, timeSlots, request.getAttendees());
    timeSlots = deleteUnqualifiedQuery(timeSlots, request.getDuration());

    // Save the result in timeSlotsForRequired
    // in case no time slot can be found when optional attendees are included
    List<TimeRange> timeSlotsForRequired = new ArrayList<TimeRange>(timeSlots);

    // Check for the availability of optional attendees
    timeSlots = queryForEvent(events, timeSlots, request.getOptionalAttendees());
    timeSlots = deleteUnqualifiedQuery(timeSlots, request.getDuration());

    if (timeSlots.isEmpty() && !request.getAttendees().isEmpty()) {
      return timeSlotsForRequired;
    }
    return timeSlots;
  }

  // Iterate through all event to check
  // if a request attendee is attending a certain event
  public List<TimeRange> queryForEvent(Collection<Event> events,
      List<TimeRange> timeSlots, Collection<String> attendees) {
    for (Iterator<Event> i = events.iterator(); i.hasNext();) {
      Event event = i.next();

      // Iterate through attendees to check the events each of them are attending
      for (Iterator<String> j = attendees.iterator(); j.hasNext();) {
        String attendee = j.next();
        if (event.getAttendees().contains(attendee)) {

          // Iterate through all time periods to see
          // if any period overlaps with an event the attendees have
          for (ListIterator<TimeRange> k = timeSlots.listIterator(); k.hasNext();) {
            TimeRange currTime = k.next();
            TimeRange eventTime = event.getWhen();

            // Removes the time period when completely covered by event time
            if (currTime.equals(eventTime) || eventTime.contains(currTime)) {
              k.remove();

            // Case B: |-----B------|
            // Event:      |---|
            //         |-1-|   |-2--|
            // Split time period from B into 1 and 2
            } else if (currTime.contains(eventTime)) {
              k.remove();
              k.add(TimeRange.fromStartEnd(currTime.start(), eventTime.start(), false));
              k.add(TimeRange.fromStartEnd(eventTime.end(), currTime.end(), false));
            
            // Case C: |--C---|
            // Event:      |------|
            //         |-1-|
            // Shorten time period from C to 1
            } else if (currTime.overlaps(eventTime)) {
              if (TimeRange.ORDER_BY_START.compare(currTime, eventTime) < 0) {
                k.remove();
                k.add(TimeRange.fromStartEnd(currTime.start(), eventTime.start(), false));
              } else {
                k.remove();
                k.add(TimeRange.fromStartEnd(eventTime.end(), currTime.end(), false));
              }
            }
          }
        }
      }
    }
    return timeSlots;
  }
  
  // Delete any time slots that has a duration less than the request time
  public List<TimeRange> deleteUnqualifiedQuery(List<TimeRange> timeSlots, 
      long duration) {
    for (ListIterator<TimeRange> k = timeSlots.listIterator(); k.hasNext();) {
      TimeRange time = k.next();
      if (time.duration() < duration) {
        k.remove();
      }
    }
    return timeSlots;
  }
}
