package gui;

import dao.EventDAO;
import dao.ParticipantDAO;
import model.Event;
import model.Participant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ParticipantPanel extends JPanel {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final EventDAO eventDAO = new EventDAO();
    private final ParticipantDAO participantDAO = new ParticipantDAO();

    private List<Event> events = new ArrayList<>();
    private List<Participant> parts = new ArrayList<>();
    private final List<Participant> shown = new ArrayList<>();
    private Map<Integer, Integer> counts = new HashMap<>();
    private final Map<Integer, Event> eventById = new HashMap<>();
    private boolean loading;

    // Register tab
    private final JComboBox<Event> eventCombo = new JComboBox<>();
    private final JLabel dateValue = valueBox(), venueValue = valueBox(), seatsValue = valueBox();
    private UIStyle.Field nameField, emailField, phoneField;
    private final JLabel nameMsg = UIStyle.msgLabel(), emailMsg = UIStyle.msgLabel(), phoneMsg = UIStyle.msgLabel();

    // View tab
    private final JComboBox<Object> filterCombo = new JComboBox<>();
    private UIStyle.Field searchField;
    private final JLabel summary = UIStyle.mutedLabel(" ");
    private DefaultTableModel tableModel;

    private final TabButton[] tabButtons = new TabButton[2];
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    public ParticipantPanel() {
        this(0);
    }

    public ParticipantPanel(int initialTab) {
        setOpaque(false);
        setLayout(new BorderLayout());

        String[] labels = {"Register Participant", "View Participants"};
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
        cards.add(buildRegisterTab(), "0");
        cards.add(buildViewTab(), "1");
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

    /** Shows event name, plus seats left / (Full) in red when showSeats is on. */
    private class EventRenderer extends DefaultListCellRenderer {
        private final boolean showSeats;
        EventRenderer(boolean showSeats) { this.showSeats = showSeats; }

        @Override
        public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
            super.getListCellRendererComponent(l, v, i, sel, foc);
            if (v instanceof Event) {
                Event ev = (Event) v;
                String t = ev.getEventName();
                if (showSeats) {
                    int left = ev.getCapacity() - counts.getOrDefault(ev.getEventId(), 0);
                    t += left <= 0 ? "  (Full)" : "  -  " + left + " seats left";
                    if (left <= 0 && !sel) setForeground(UIStyle.DANGER);
                }
                setText(t);
            }
            return this;
        }
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

    // ---------- Register ----------
    private JPanel buildRegisterTab() {
        eventCombo.setFont(UIStyle.LABEL_FONT);
        eventCombo.setRenderer(new EventRenderer(true));
        eventCombo.addActionListener(e -> { if (!loading) updateEventInfo(); });

        nameField = new UIStyle.Field("Full name", 20);
        emailField = new UIStyle.Field("name@example.com", 20);
        phoneField = new UIStyle.Field("10-digit phone", 20);
        UIStyle.onChange(nameField, () -> UIStyle.setCheck(nameMsg, nameField.getText().isBlank(), true, ""));
        UIStyle.onChange(emailField, () -> UIStyle.setCheck(emailMsg, emailField.getText().isBlank(),
                EMAIL_PATTERN.matcher(emailField.getText().trim()).matches(), "Enter a valid email"));
        UIStyle.onChange(phoneField, () -> UIStyle.setCheck(phoneMsg, phoneField.getText().isBlank(),
                phoneField.getText().trim().matches("\\d{10}"), "Phone must be exactly 10 digits"));

        eventCombo.setBackground(Color.WHITE);
        eventCombo.setPreferredSize(new Dimension(100, 42));

        JPanel dateVenue = new JPanel(new GridLayout(1, 2, 12, 0));
        dateVenue.setOpaque(false);
        dateVenue.add(UIStyle.labeled("Date", dateValue, UIStyle.msgLabel()));
        dateVenue.add(UIStyle.labeled("Venue", venueValue, UIStyle.msgLabel()));

        // One shared grid: each row holds a left cell and a right cell, so every row lines up exactly.
        JPanel cols = new JPanel(new GridLayout(0, 2, 28, 8));
        cols.setOpaque(false);
        cols.add(UIStyle.sectionTitle("Choose Event"));
        cols.add(UIStyle.sectionTitle("Participant Details"));
        cols.add(UIStyle.labeled("Event", eventCombo, UIStyle.msgLabel()));
        cols.add(UIStyle.labeled("Name", nameField, nameMsg));
        cols.add(dateVenue);
        cols.add(UIStyle.labeled("Email", emailField, emailMsg));
        cols.add(UIStyle.labeled("Seats", seatsValue, UIStyle.msgLabel()));
        cols.add(UIStyle.labeled("Phone", phoneField, phoneMsg));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(cols, BorderLayout.NORTH);

        JButton register = UIStyle.successButton("Register");
        register.addActionListener(e -> registerParticipant());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btns.setOpaque(false);
        btns.add(register);

        UIStyle.Card card = new UIStyle.Card(new BorderLayout(0, 16));
        card.add(top, BorderLayout.CENTER);
        card.add(btns, BorderLayout.SOUTH);
        return UIStyle.page(card);
    }

    /** Read-only value box styled like the text fields so both columns line up. */
    private static JLabel valueBox() {
        JLabel l = new JLabel(" ");
        l.setFont(UIStyle.LABEL_FONT);
        l.setForeground(UIStyle.TEXT);
        l.setOpaque(true);
        l.setBackground(new Color(0xF3F4F6));
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                new EmptyBorder(6, 10, 6, 10)));
        return l;
    }

    private void updateEventInfo() {
        Event ev = (Event) eventCombo.getSelectedItem();
        if (ev == null) {
            dateValue.setText("-");
            venueValue.setText("-");
            seatsValue.setForeground(UIStyle.MUTED);
            seatsValue.setText("No events yet. Create one first.");
            return;
        }
        int left = ev.getCapacity() - counts.getOrDefault(ev.getEventId(), 0);
        dateValue.setText(String.valueOf(ev.getEventDate()));
        venueValue.setText(String.valueOf(ev.getVenue()));
        seatsValue.setForeground(left <= 0 ? UIStyle.DANGER : UIStyle.TEXT);
        seatsValue.setText(left <= 0 ? "FULL" : left + " of " + ev.getCapacity() + " seats left");
    }

    private void registerParticipant() {
        Event ev = (Event) eventCombo.getSelectedItem();
        if (ev == null) { UIStyle.toast(this, "Create an event first", false); return; }
        if (ev.getCapacity() - counts.getOrDefault(ev.getEventId(), 0) <= 0) {
            UIStyle.toast(this, "This event is full", false);
            return;
        }
        String name = nameField.getText().trim(), email = emailField.getText().trim(), phone = phoneField.getText().trim();
        boolean okN = !name.isEmpty(), okE = EMAIL_PATTERN.matcher(email).matches(), okP = phone.matches("\\d{10}");
        if (!okN) UIStyle.setCheck(nameMsg, false, false, "Name is required");
        if (!okE) UIStyle.setCheck(emailMsg, false, false, "Enter a valid email");
        if (!okP) UIStyle.setCheck(phoneMsg, false, false, "Phone must be exactly 10 digits");
        if (!(okN && okE && okP)) return;
        try {
            participantDAO.registerParticipant(new Participant(ev.getEventId(), name, email, phone));
            UIStyle.toast(this, name + " registered for " + ev.getEventName(), true);
            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            loadData();
        } catch (SQLException ex) {
            UIStyle.toast(this, "Database error: " + ex.getMessage(), false);
        }
    }

    // ---------- View ----------
    private JPanel buildViewTab() {
        filterCombo.setFont(UIStyle.LABEL_FONT);
        filterCombo.setRenderer(new EventRenderer(false));
        filterCombo.addActionListener(e -> { if (!loading) applyFilter(); });
        searchField = new UIStyle.Field("Search name, email or phone...", 20);
        UIStyle.onChange(searchField, this::applyFilter);

        JPanel controls = new JPanel(new BorderLayout(10, 0));
        controls.setOpaque(false);
        controls.add(filterCombo, BorderLayout.WEST);
        controls.add(searchField, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(new Object[]{"Name", "Email", "Phone", "Event", "ID"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        UIStyle.styleTable(table);
        UIStyle.decorateColumn(table, 0, false, true);
        table.getColumnModel().getColumn(4).setMaxWidth(50);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER));
        sp.getViewport().setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(controls, BorderLayout.NORTH);
        top.add(summary, BorderLayout.SOUTH);
        UIStyle.Card card = new UIStyle.Card(new BorderLayout(0, 12));
        card.add(top, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return UIStyle.page(card);
    }

    private String eventName(int id) {
        Event e = eventById.get(id);
        return e == null ? "Event #" + id : e.getEventName();
    }

    public void loadData() {
        loading = true;
        try {
            Event cur = (Event) eventCombo.getSelectedItem();
            Object curFilter = filterCombo.getSelectedItem();
            int selId = cur == null ? -1 : cur.getEventId();
            int fId = curFilter instanceof Event ? ((Event) curFilter).getEventId() : -1;

            events = eventDAO.getAllEvents();
            parts = participantDAO.getAllParticipants();
            counts = UIStyle.countByEvent(parts);
            eventById.clear();
            eventCombo.removeAllItems();
            filterCombo.removeAllItems();
            filterCombo.addItem("All events");
            for (Event ev : events) {
                eventById.put(ev.getEventId(), ev);
                eventCombo.addItem(ev);
                filterCombo.addItem(ev);
                if (ev.getEventId() == selId) eventCombo.setSelectedItem(ev);
                if (ev.getEventId() == fId) filterCombo.setSelectedItem(ev);
            }
        } catch (SQLException ex) {
            UIStyle.toast(this, "Database error: " + ex.getMessage(), false);
        } finally {
            loading = false;
        }
        updateEventInfo();
        applyFilter();
    }

    private void applyFilter() {
        if (tableModel == null) return;
        Object sel = filterCombo.getSelectedItem();
        Integer eid = sel instanceof Event ? ((Event) sel).getEventId() : null;
        String q = searchField.getText().trim().toLowerCase();
        shown.clear();
        tableModel.setRowCount(0);
        for (Participant p : parts) {
            if (eid != null && p.getEventId() != eid.intValue()) continue;
            if (!q.isEmpty() && !(p.getParticipantName().toLowerCase().contains(q)
                    || p.getEmail().toLowerCase().contains(q) || p.getPhone().contains(q))) continue;
            shown.add(p);
            tableModel.addRow(new Object[]{p.getParticipantName(), p.getEmail(), p.getPhone(),
                    eventName(p.getEventId()), p.getParticipantId()});
        }
        int n = shown.size();
        summary.setText(n + " participant" + (n == 1 ? "" : "s") + " in " + (eid == null ? "all events" : eventName(eid)));
    }
}