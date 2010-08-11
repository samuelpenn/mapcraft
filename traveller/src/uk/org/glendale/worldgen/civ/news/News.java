package uk.org.glendale.worldgen.civ.news;

import javax.persistence.Entity;

/**
 * Defines a news item. News travels around the galaxy at the speed of the
 * fastest ship. When an event occurs on a world, a record of the event is
 * kept in the system. A copy of the record is given to each ship that
 * passes through, and when a ship arrives at a new system, any 'new' news
 * events are copied to that system's repository.
 * 
 * This way events will slowly percolate around the galaxy. Records are
 * deleted over time, and the importance of the initial event and the distance
 * from the source affect how quickly.
 * 
 * There will be one original copy of any news event (originalId = 0), and
 * zero or more copies (originalId = id of the original instance). Each
 * ship and star system will have its own copy of each event.
 * 
 * We may only keep one copy of the actual message text (in the original),
 * in order to save on storage space.
 * 
 * @author Samuel Penn
 */
@Entity
public class News {

}
