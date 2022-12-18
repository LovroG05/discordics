package ml.perchperkins.discordics.controllers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.Objects;

public class CalendarController implements Route {
	private final JDA jda;

	public CalendarController(JDA jda) {
		this.jda = jda;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//Discord ICAL//iCal4j 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);

		List<ScheduledEvent> events = jda.getScheduledEvents();
		System.out.println("found " + events.size() + " event/s");

		events.forEach(event -> {
			String eventName = event.getName();
			System.out.println(eventName);

			long eventStart = event.getStartTime().toInstant().getEpochSecond();
			long eventEnd = eventStart + 3600;
			if(!Objects.isNull(event.getEndTime())) {
				eventEnd = event.getEndTime().toInstant().getEpochSecond();
			}

			VEvent dogodek = new VEvent(
					new DateTime(eventStart),
					new DateTime(eventEnd),
					eventName
			);

			System.out.println("d: " + dogodek);

			String location = switch(event.getType()) {
				case EXTERNAL -> event.getLocation();
				case VOICE, STAGE_INSTANCE -> Objects.requireNonNull(event.getChannel()).getName();
				default -> "";
			};
			System.out.println(location);

			Location locationProperty = new Location(event.getGuild().getName() + " " + location);
			dogodek.getProperties().add(locationProperty);

			try {
				UidGenerator ug = new UidGenerator("1");
				dogodek.getProperties().add(ug.generateUid());
			} catch(Exception e) {
				e.printStackTrace();
			}

			calendar.getComponents().add(dogodek);
		});

		System.out.println(calendar);
		response.header("Content-Disposition", "attachment;filename=cal.ics");
		response.header("Content-Type", "text/calendar;charset=utf-8;");
		return calendar.toString();
	}
}
