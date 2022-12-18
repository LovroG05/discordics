package ml.perchperkins;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
import spark.Spark;


import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        final String token = dotenv.get("TOKEN");
        JDA jda = JDABuilder.createDefault(token).build();

        Spark.get("/calendar", (request, response) -> {
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
                if (!Objects.isNull(event.getEndTime())) {
                    eventEnd = event.getEndTime().toInstant().getEpochSecond();
                }

                VEvent dogodek = new VEvent(
                        new DateTime(eventStart),
                        new DateTime(eventEnd),
                        eventName
                );

                System.out.println("d: " + dogodek);

                String location = "";
                if (event.getType() == ScheduledEvent.Type.EXTERNAL) {
                    location = event.getLocation();
                } else if (event.getType() == ScheduledEvent.Type.VOICE || event.getType() == ScheduledEvent.Type.STAGE_INSTANCE) {
                    location = Objects.requireNonNull(event.getChannel()).getName();
                } else {
                    location = "";
                }

                System.out.println(location);

                Location locationProperty = new Location(event.getGuild().getName() + " " + location);
                dogodek.getProperties().add(locationProperty);

                UidGenerator ug = null;
                try {
                    ug = new UidGenerator("1");
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
                dogodek.getProperties().add(ug.generateUid());


                calendar.getComponents().add(dogodek);
            });

            System.out.println(calendar);
            response.header("Content-Disposition", "attachment;filename=cal.ics");
            return calendar.toString();
        });
    }
}