package gui;

import dao.EventDAO;
import dao.ParticipantDAO;
import model.Event;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventPanel extends JPanel {

    private final EventDAO eventDAO = new EventDAO();
    private final ParticipantDAO participantDAO = new ParticipantDAO();

    private UIStyle.Field nameField, dateField, venueField, searchField;
    private JSpinner capacity;
    private final JLabel nameMsg = UIStyle.msgLabel(), dateMsg = UIStyle.msgLabel(), venueMsg = UIStyle.msgLabel();
    private final JLabel viewCount = UIStyle.mutedLabel(" "), searchCount = UIStyle.mutedLabel(" ");
    private DefaultTableModel viewModel, searchModel;

    private List<Event> allEvents = new ArrayList<>();
    private Map<Integer, Integer> counts = new HashMap<>();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final TabButton[] tabButtons = new TabButton[3];

    public EventPanel() {
        this(0);
    }

    public EventPanel(int initialTab) {
        setOpaque(false);
        setLayout(new BorderLayout());

        String[] labels = {"Create Event", "View Events", "Search Events"};
        JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tabRow.setOpaque(false);
        for (int i = 0; i < labels.length; i++) {
            int idx = i;
            TabButton b = new TabButton(labels[i], i == initialTab);
            b.addActionListener(e -> selectTab(idx));
            tabButtons[i] = b;
            tabRow.add(b);
        }
        JPanel tabBar = new JPanel(new BorderLayout());
        tabBar.setOpaque(true);
        tabBar.setBackground(Color.WHITE);
        tabBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.BORDER));
        tabBar.add(tabRow, BorderLayout.WEST);

        cards.setOpaque(false);
        cards.add(buildCreateTab(), "0");
        cards.add(buildViewTab(), "1");
        cards.add(buildSearchTab(), "2");

        add(tabBar, BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);

        if (initialTab >= 0 && initialTab < labels.length) {
            cardLayout.show(cards, String.valueOf(initialTab));
        }
        loadData();
    }

    public void selectTab(int index) {
        if (index < 0 || index >= tabButtons.length) return;
        for (int i = 0; i < tabButtons.length; i++) {
            tabButtons[i].setActiveState(i == index);
        }
        cardLayout.show(cards, String.valueOf(index));
    }

    /** Underline-style tab button: coral red text + underline when active, muted gray otherwise. */
    private static class TabButton extends JButton {
        private boolean active;

        TabButton(String text, boolean active) {
            super(text);
            this.active = active;
            setFont(UIStyle.BUTTON_FONT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(12, 18, 12, 18));
            applyColor();
        }

        void setActiveState(boolean a) {
            active = a;
            applyColor();
            repaint();
        }

        private void applyColor() {
            setForeground(active ? UIStyle.PRIMARY : UIStyle.MUTED);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (active) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIStyle.aa(g2);
                g2.setColor(UIStyle.PRIMARY);
                g2.fillRoundRect(0, getHeight() - 3, getWidth(), 3, 3, 3);
                g2.dispose();
            }
        }
    }

    // ---------- Create ----------
    private JPanel buildCreateTab() {
        nameField = new UIStyle.Field("e.g. Tech Fest 2026", 20);
        dateField = new UIStyle.Field("yyyy-MM-dd", 20);
        venueField = new UIStyle.Field("e.g. Main Auditorium", 20);
        capacity = new JSpinner(new SpinnerNumberModel(50, 1, 100000, 1));
        capacity.setFont(UIStyle.LABEL_FONT);

        JPanel form = new JPanel(new GridLayout(0, 2, 24, 8));
        form.setOpaque(false);
        form.add(UIStyle.labeled("Event name", nameField, nameMsg));
        form.add(UIStyle.labeled("Date", dateField, dateMsg));
        form.add(UIStyle.labeled("Venue", venueField, venueMsg));
        form.add(UIStyle.labeled("Capacity (seats)", capacity, UIStyle.msgLabel()));
        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.setOpaque(false);
        formWrap.add(form, BorderLayout.NORTH);

        JButton clear = UIStyle.secondaryButton("Clear");
        clear.addActionListener(e -> clearForm());
        JButton create = UIStyle.primaryButton("Create Event");
        create.addActionListener(e -> createEvent());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        btns.add(clear);
        btns.add(create);

        UIStyle.Card card = new UIStyle.Card(new BorderLayout(0, 16));
        card.add(UIStyle.sectionTitle("Event details"), BorderLayout.NORTH);
        card.add(formWrap, BorderLayout.CENTER);
        card.add(btns, BorderLayout.SOUTH);
        return UIStyle.page(card);
    }

    private void fail(JTextField f, JLabel l, String msg) {
        UIStyle.markInvalid(f);
        UIStyle.setCheck(l, false, false, msg);
    }

    private void pass(JTextField f, JLabel l) {
        UIStyle.styleTextField(f);
        l.setText(" ");
    }

    private void clearForm() {
        nameField.setText("");
        dateField.setText("");
        venueField.setText("");
        capacity.setValue(50);
        pass(nameField, nameMsg);
        pass(dateField, dateMsg);
        pass(venueField, venueMsg);
    }

    private void createEvent() {
        String name = nameField.getText().trim(), date = dateField.getText().trim(), venue = venueField.getText().trim();
        boolean ok = true;
        if (name.isEmpty()) { fail(nameField, nameMsg, "Event name is required"); ok = false; } else pass(nameField, nameMsg);
        if (venue.isEmpty()) { fail(venueField, venueMsg, "Venue is required"); ok = false; } else pass(venueField, venueMsg);
        try {
            LocalDate parsed = LocalDate.parse(date);
            if (parsed.isBefore(LocalDate.now())) {
                fail(dateField, dateMsg, "Event date cannot be in the past");
                ok = false;
            } else {
                pass(dateField, dateMsg);
            }
        } catch (DateTimeParseException ex) {
            fail(dateField, dateMsg, "Use yyyy-MM-dd, e.g. 2026-11-10");
            ok = false;
        }
        if (!ok) return;
        try {
            eventDAO.createEvent(new Event(name, date, venue, (Integer) capacity.getValue()));
            UIStyle.toast(this, "Event created", true);
            clearForm();
            loadData();
        } catch (IllegalArgumentException ex) {
            fail(dateField, dateMsg, ex.getMessage());
        } catch (SQLException ex) {
            UIStyle.toast(this, "Database error: " + ex.getMessage(), false);
        }
    }

    // ---------- View ----------
    private JPanel buildViewTab() {
        viewModel = newModel();
        JTable table = newTable(viewModel);
        JButton refresh = UIStyle.secondaryButton("Refresh");
        refresh.addActionListener(e -> loadData());
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(viewCount, BorderLayout.WEST);
        top.add(refresh, BorderLayout.EAST);

        UIStyle.Card card = new UIStyle.Card(new BorderLayout(0, 12));
        card.add(top, BorderLayout.NORTH);
        card.add(scroll(table), BorderLayout.CENTER);
        return UIStyle.page(card);
    }

    // ---------- Search (live filter) ----------
    private JPanel buildSearchTab() {
        searchModel = newModel();
        JTable table = newTable(searchModel);
        searchField = new UIStyle.Field("Search by name or venue...", 30);
        UIStyle.onChange(searchField, this::applyFilter);
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        top.add(searchField, BorderLayout.CENTER);
        top.add(searchCount, BorderLayout.EAST);

        UIStyle.Card card = new UIStyle.Card(new BorderLayout(0, 12));
        card.add(top, BorderLayout.NORTH);
        card.add(scroll(table), BorderLayout.CENTER);
        return UIStyle.page(card);
    }

    // ---------- Shared ----------
    private DefaultTableModel newModel() {
        return new DefaultTableModel(new Object[]{"ID", "Event", "Date", "Venue", "Seats", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable newTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        UIStyle.styleTable(t);
        t.getColumnModel().getColumn(0).setMaxWidth(50);
        UIStyle.decorateColumn(t, 5, true, false);
        return t;
    }

    private JScrollPane scroll(JTable t) {
        JScrollPane s = new JScrollPane(t);
        s.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER));
        s.getViewport().setBackground(Color.WHITE);
        return s;
    }

    private void fill(DefaultTableModel m, List<Event> list) {
        m.setRowCount(0);
        for (Event ev : list) {
            int used = counts.getOrDefault(ev.getEventId(), 0);
            m.addRow(new Object[]{ev.getEventId(), ev.getEventName(), ev.getEventDate(), ev.getVenue(),
                    used + " / " + ev.getCapacity(), UIStyle.statusText(used, ev.getCapacity())});
        }
    }

    public void loadData() {
        try {
            allEvents = eventDAO.getAllEvents();
            counts = UIStyle.countByEvent(participantDAO.getAllParticipants());
            fill(viewModel, allEvents);
            viewCount.setText(allEvents.isEmpty() ? "No events yet. Use “Create Event” to get started." : allEvents.size() + " events");
            applyFilter();
        } catch (SQLException ex) {
            UIStyle.toast(this, "Database error: " + ex.getMessage(), false);
        }
    }

    private void applyFilter() {
        if (searchModel == null) return;
        String q = searchField.getText().trim().toLowerCase();
        List<Event> hits = new ArrayList<>();
        for (Event ev : allEvents) {
            if (q.isEmpty() || ev.getEventName().toLowerCase().contains(q) || ev.getVenue().toLowerCase().contains(q)) hits.add(ev);
        }
        fill(searchModel, hits);
        searchCount.setText(hits.isEmpty() && !q.isEmpty() ? "No events match “" + searchField.getText().trim() + "”"
                : hits.size() + " events found");
    }
}