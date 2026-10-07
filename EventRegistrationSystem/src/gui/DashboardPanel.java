package gui;

import dao.EventDAO;
import dao.ParticipantDAO;
import model.Event;
import model.Participant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Home page: welcome banner, 4 stat cards, upcoming events with seat bars, registrations chart. */
public class DashboardPanel extends JPanel {

    private final EventDAO eventDAO = new EventDAO();
    private final ParticipantDAO participantDAO = new ParticipantDAO();
    private final JLabel[] values = new JLabel[4];
    private final JPanel upcomingList = new JPanel(new GridLayout(0, 1, 0, 14));

    public DashboardPanel(Runnable onNewEvent, Runnable onRegister) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(banner(onNewEvent, onRegister));
        top.add(Box.createVerticalStrut(18));
        top.add(statsRow());
        add(top, BorderLayout.NORTH);

        upcomingList.setOpaque(false);
        UIStyle.Card upcoming = new UIStyle.Card(new BorderLayout(0, 10));
        upcoming.add(UIStyle.sectionTitle("Upcoming events"), BorderLayout.NORTH);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(upcomingList, BorderLayout.NORTH);
        upcoming.add(wrap, BorderLayout.CENTER);

        add(upcoming, BorderLayout.CENTER);

        refresh();
    }

    // ---- Welcome banner with gradient ----
    private JPanel banner(Runnable onNewEvent, Runnable onRegister) {
        JPanel b = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIStyle.aa(g2);
                g2.setPaint(new GradientPaint(0, 0, UIStyle.SIDEBAR, getWidth(), 0, UIStyle.PRIMARY));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setPaint(new GradientPaint(0, 0, UIStyle.SIDEBAR, getWidth(), 0, UIStyle.SECONDARY));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(22, 26, 22, 26));

        JLabel hi = new JLabel("Welcome to EventHub");
        hi.setFont(new Font("Segoe UI", Font.BOLD, 24));
        hi.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Plan Events. Connect People. Create Memories.");
        sub.setFont(UIStyle.LABEL_FONT);
        sub.setForeground(new Color(0xE2E8F0));
        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.setAlignmentX(Component.LEFT_ALIGNMENT);
        hi.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(hi);
        text.add(Box.createVerticalStrut(4));
        text.add(sub);

        JButton newEvent = UIStyle.primaryButton("+ New Event");
        newEvent.addActionListener(e -> onNewEvent.run());
        JButton register = UIStyle.secondaryButton("+ Register Participant");
        register.addActionListener(e -> onRegister.run());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btns.setOpaque(false);
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        btns.add(newEvent);
        btns.add(register);

        // Stack text above buttons instead of side-by-side, so they can
        // never overlap when the window is narrowed.
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(text);
        content.add(Box.createVerticalStrut(14));
        content.add(btns);
        b.add(content, BorderLayout.WEST);
        return b;
    }

    // ---- Four stat cards ----
    private JPanel statsRow() {
        String[] titles = {"Total Events", "Participants", "Upcoming Events", "Seats Left"};
        Color[] colors = {UIStyle.PRIMARY, UIStyle.SECONDARY, UIStyle.WARNING, UIStyle.ACCENT};
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            UIStyle.Card c = new UIStyle.Card(new BorderLayout(0, 4));
            JLabel t = UIStyle.mutedLabel(titles[i]);
            values[i] = new JLabel("0");
            values[i].setFont(new Font("Segoe UI", Font.BOLD, 32));
            values[i].setForeground(colors[i]);
            c.add(t, BorderLayout.NORTH);
            c.add(values[i], BorderLayout.CENTER);
            row.add(c);
        }
        return row;
    }

    /** Reload numbers and upcoming list from the database. */
    public void refresh() {
        try {
            List<Event> events = eventDAO.getAllEvents();
            List<Participant> parts = participantDAO.getAllParticipants();
            Map<Integer, Integer> counts = UIStyle.countByEvent(parts);
            LocalDate today = LocalDate.now();

            int seatsLeft = 0, upcomingCount = 0;
            List<Event> soon = new ArrayList<>();
            for (Event ev : events) {
                int reg = counts.getOrDefault(ev.getEventId(), 0);
                seatsLeft += Math.max(0, ev.getCapacity() - reg);
                LocalDate d = UIStyle.parseDate(ev.getEventDate());
                if (d != null && !d.isBefore(today)) {
                    upcomingCount++;
                    soon.add(ev);
                }
            }
            soon.sort((a, b) -> UIStyle.parseDate(a.getEventDate()).compareTo(UIStyle.parseDate(b.getEventDate())));

            values[0].setText(String.valueOf(events.size()));
            values[1].setText(String.valueOf(parts.size()));
            values[2].setText(String.valueOf(upcomingCount));
            values[3].setText(String.valueOf(seatsLeft));

            upcomingList.removeAll();
            if (soon.isEmpty()) {
                upcomingList.add(UIStyle.mutedLabel("No upcoming events. Click “+ New Event” to get started."));
            }
            for (int i = 0; i < Math.min(5, soon.size()); i++) {
                Event ev = soon.get(i);
                upcomingList.add(eventRow(ev, counts.getOrDefault(ev.getEventId(), 0)));
            }
            upcomingList.revalidate();
            upcomingList.repaint();
        } catch (SQLException ex) {
            UIStyle.toast(this, "Database error: " + ex.getMessage(), false);
        }
    }

    // ---- Mini card for an upcoming event ----
    private JPanel eventRow(Event ev, int used) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel name = new JLabel(ev.getEventName());
        name.setFont(UIStyle.BUTTON_FONT);
        name.setForeground(UIStyle.TEXT);
        JLabel meta = UIStyle.mutedLabel("Date: " + ev.getEventDate() + "   Venue: " + ev.getVenue());
        JPanel txt = new JPanel();
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.setOpaque(false);
        txt.add(name);
        txt.add(Box.createVerticalStrut(6));
        txt.add(meta);

        UIStyle.SeatBar bar = new UIStyle.SeatBar();
        bar.set(used, ev.getCapacity());
        JLabel seats = UIStyle.mutedLabel(used + " / " + ev.getCapacity() + " seats");
        JPanel right = new JPanel(new BorderLayout(0, 4));
        right.setOpaque(false);
        right.add(seats, BorderLayout.NORTH);
        right.add(bar, BorderLayout.CENTER);

        row.add(txt, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }
}