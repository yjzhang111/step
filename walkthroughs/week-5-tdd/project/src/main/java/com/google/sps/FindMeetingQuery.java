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


/**
 * Find time slot(s) that all the required attendees are available
 * Include optional attendees if at least one time slot can be found
 * Otherwise return the time slots that fit just the required attendees.

 * When there is no required attendee, return the time slots that fit
 * all optional attendees.

 * Return an empty list if no time slot is found.
 */
public final class FindMeetingQuery {
  public Collection<TimeRange> query(List<Event> events, MeetingRequest request) {
    // Initialize a time slot (the whole day)
    List<TimeRange> timeSlots = new ArrayList<>();
    timeSlots.add(TimeRange.WHOLE_DAY);

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

  // Iterate through all events to check
  // if a request attendee is attending a certain event
  public List<TimeRange> queryForEvent(List<Event> events,
      List<TimeRange> timeSlots, Collection<String> attendees) {
    for (Event event: events) {

      // Iterate through attendees to check the events each of them are attending
      for (Iterator<String> j = attendees.iterator(); j.hasNext();) {
        String attendee = j.next();
        if (event.getAttendees().contains(attendee)) {

          // Iterate through all time slots to see
          // if any period overlaps with an event the attendees have
          for (ListIterator<TimeRange> k = timeSlots.listIterator(); k.hasNext();) {
            TimeRange currTimeSlot = k.next();
            TimeRange eventTime = event.getWhen();

            // Removes the time slot when completely covered by event time
            if (currTimeSlot.equals(eventTime) || eventTime.contains(currTimeSlot)) {
              k.remove();

            // currTimeSlot: |-----B------|
            // eventTime   :     |---|
            //               |-1-|   |-2--|
            // Split current time slot from B into 1 and 2
            } else if (currTimeSlot.contains(eventTime)) {
              k.remove();
              k.add(TimeRange.fromStartEnd(currTimeSlot.start(), eventTime.start(), false));
              k.add(TimeRange.fromStartEnd(eventTime.end(), currTimeSlot.end(), false));
            
            // currTimeSlot: |--C---|
            // eventTime   :     |------|
            //               |-1-|
            // Shorten current time slot from C to 1
            } else if (currTimeSlot.overlaps(eventTime)) {
              if (TimeRange.ORDER_BY_START.compare(currTimeSlot, eventTime) < 0) {
                k.remove();
                k.add(TimeRange.fromStartEnd(currTimeSlot.start(), eventTime.start(), false));
              } else {
                k.remove();
                k.add(TimeRange.fromStartEnd(eventTime.end(), currTimeSlot.end(), false));
              }
            }
          }
        }
      }
    }
    return timeSlots;
  }
  
  // Delete any time slots that have a duration less than the request time
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
