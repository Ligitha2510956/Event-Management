package gui;

import dao.EventDAO;
import dao.RegistrationDAO;
import model.Event;
import model.Participant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegistrationPanel extends JPanel {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Color CHANGED = new Color(0xFEF9C3);   // light yellow
    private static final Color DISABLED = new Color(0xF3F4F6);

    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private final EventDAO eventDAO = new EventDAO();

    // Modify tab
    private UIStyle.Field modifyId, nameField, emailField, phoneField;
    private final JLabel profile = new JLabel("Load a participant to edit their details");
    private final JLabel emailMsg = UIStyle.msgLabel(), phoneMsg = UIStyle.msgLabel(), nameMsg = UIStyle.msgLabel();
    private Participant loaded;
    private String origName = "", origEmail = "", origPhone = "";

    // Cancel tab
    private UIStyle.Field cancelId;
    private final JLabel details = UIStyle.mutedLabel("Find a participant to see their registration");
    private JButton cancelBtn;
    private Participant cancelTarget;
    private String cancelEventName = "";

    private final TabButton[] tabButtons = new TabButton[2];
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    public RegistrationPanel() {
        this(0);
    }

    public RegistrationPanel(int initialTab) {
        setOpaque(false);
        setLayout(new BorderLayout());

        String[] labels = {"Modify Registration", "Cancel Registration"};
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
        cards.add(buildModifyTab(), "0");
        cards.add(buildCancelTab(), "1");
        add(tabBar, BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);
        if (initialTab >= 0 && initialTab < labels.length) {
            cardLayout.show(cards, String.valueOf(initialTab));
        }
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

    // ---------- Modify ----------
    private JPanel buildModifyTab() {
        modifyId = new UIStyle.Field("Participant ID", 12);
        nameField = new UIStyle.Field("Full name", 20);
        emailField = new UIStyle.Field("name@example.com", 20);
        phoneField = new UIStyle.Field("10-digit phone", 20);
        setFieldsEnabled(false);

        JButton load = UIStyle.primaryButton("Load");
        load.addActionListener(e -> loadParticipant());
        modifyId.addActionListener(e -> loadParticipant());
        JPanel search = new JPanel(new BorderLayout(10, 0));
        search.setOpaque(false);
        search.add(modifyId, BorderLayout.CENTER);
        search.add(load, BorderLayout.EAST);

        profile.setFont(UIStyle.LABEL_FONT);
        profile.setForeground(UIStyle.MUTED);
        profile.setIconTextGap(14);
        profile.setBorder(new EmptyBorder(6, 0, 6, 0));

        UIStyle.onChange(nameField, () -> {
            UIStyle.setCheck(nameMsg, !nameField.isEnabled() || nameField.getText().isBlank(), true, "");
            refreshHighlights();
        });
        UIStyle.onChange(emailField, () -> {
            UIStyle.setCheck(emailMsg, !emailField.isEnabled() || emailField.getText().isBlank(),
                    EMAIL_PATTERN.matcher(emailField.getText().trim()).matches(), "Enter a valid email");
            refreshHighlights();
        });
        UIStyle.onChange(phoneField, () -> {
            String p = phoneField.getText().trim();
            boolean ok = p.matches("\\d{10}") || p.matches("\\+?\\d{7,15}");
            UIStyle.setCheck(phoneMsg, !phoneField.isEnabled() || phoneField.getText().isBlank(),
                    ok, "Enter a valid phone number (7-15 digits)");
            refreshHighlights();
        });

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setOpaque(false);
        form.add(profile);
        form.add(UIStyle.labeled("Name", nameField, nameMsg));
        form.add(UIStyle.labeled("Email", emailField, emailMsg));
        form.add(UIStyle.labeled("Phone", phoneField, phoneMsg));

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(UIStyle.labeled("Find registration", search, null), BorderLayout.NORTH);
        body.add(form, BorderLayout.CENTER);

        JButton reset = UIStyle.secondaryButton("Reset");
        reset.addActionListener(e -> {
            nameField.setText(origName);
            emailField.setText(origEmail);
            phoneField.setText(origPhone);
            refreshHighlights();
        });
        JButton save = UIStyle.primaryButton("Save Changes");
        save.addActionListener(e -> updateRegistration());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btns.setOpaque(false);
        btns.add(reset);
        btns.add(Box.createHorizontalStrut(10));
        btns.add(save);

        UIStyle.Card card = new UIStyle.Card(new BorderLayout(0, 12));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(body, BorderLayout.NORTH);
        card.add(wrap, BorderLayout.CENTER);
        card.add(btns, BorderLayout.SOUTH);
        return UIStyle.page(card);
    }

    private void setFieldsEnabled(boolean on) {
        nameField.setEnabled(on);
        nameField.setEditable(on);
        emailField.setEnabled(on);
        emailField.setEditable(on);
        phoneField.setEnabled(on);
        phoneField.setEditable(on);
        refreshHighlights();
    }

    /** Yellow = edited, grey = locked, white = untouched. */
    private void refreshHighlights() {
        if (phoneField == null) return;
        paint(nameField, origName);
        paint(emailField, origEmail);
        paint(phoneField, origPhone);
    }

    private void paint(JTextField f, String orig) {
        f.setBackground(!f.isEnabled() ? DISABLED : f.getText().trim().equals(orig) ? Color.WHITE : CHANGED);
    }

    private void loadParticipant() {
        int id;
        try {
            id = Integer.parseInt(modifyId.getText().trim());
        } catch (NumberFormatException ex) {
            UIStyle.toast(this, "Participant ID must be a number", false);
            return;
        }
        try {
            loaded = registrationDAO.getParticipantById(id);
            if (loaded == null) {
                UIStyle.toast(this, "No participant found with that ID", false);
                setFieldsEnabled(false);
                profile.setIcon(null);
                profile.setText("Load a participant to edit their details");
                return;
            }
            origName = loaded.getParticipantName() != null ? loaded.getParticipantName() : "";
            origEmail = loaded.getEmail() != null ? loaded.getEmail() : "";
            origPhone = loaded.getPhone() != null ? loaded.getPhone() : "";
            setFieldsEnabled(true);
            nameField.setText(origName);
            emailField.setText(origEmail);
            phoneField.setText(origPhone);
            profile.setIcon(UIStyle.avatarIcon(origName, 44));
            profile.setForeground(UIStyle.TEXT);
            profile.setText("<html><b style='font-size:14px'>" + origName + "</b><br>Participant ID #" + id + "</html>");
            refreshHighlights();
        } catch (SQLException ex) {
            UIStyle.toast(this, "Database error: " + ex.getMessage(), false);
        }
    }

    private void updateRegistration() {
        if (loaded == null) {
            UIStyle.toast(this, "Load a participant by ID first", false);
            return;
        }
        String name = nameField.getText().trim(), email = emailField.getText().trim(), phone = phoneField.getText().trim();
        boolean okN = !name.isEmpty();
        boolean okE = EMAIL_PATTERN.matcher(email).matches();
        boolean okP = phone.matches("\\d{10}") || phone.matches("\\+?\\d{7,15}");
        if (!okN) UIStyle.setCheck(nameMsg, false, false, "Name is required");
        if (!okE) UIStyle.setCheck(emailMsg, false, false, "Enter a valid email");
        if (!okP) UIStyle.setCheck(phoneMsg, false, false, "Enter a valid phone number (7-15 digits)");
        if (!(okN && okE && okP)) return;

        loaded.setParticipantName(name);
        loaded.setEmail(email);
        loaded.setPhone(phone);
        try {
            if (registrationDAO.updateRegistration(loaded)) {
                origName = name;
                origEmail = email;
                origPhone = phone;
                refreshHighlights();
                UIStyle.toast(this, "Registration updated successfully", true);
            } else {
                UIStyle.toast(this, "Update failed. The participant may no longer exist.", false);
            }
        } catch (SQLException ex) {
            UIStyle.toast(this, "Database error: " + ex.getMessage(), false);
        }
    }

    // ---------- Cancel ----------
    private JPanel buildCancelTab() {
        JLabel warn = new JLabel("Cancelling a registration cannot be undone.");
        warn.setFont(UIStyle.BUTTON_FONT);
        warn.setForeground(new Color(0xB91C1C));
        warn.setOpaque(true);
        warn.setBackground(new Color(0xFEE2E2));
        warn.setBorder(new EmptyBorder(12, 16, 12, 16));

        cancelId = new UIStyle.Field("Participant ID", 12);
        JButton find = UIStyle.secondaryButton("Find");
        find.addActionListener(e -> findForCancel());
        cancelId.addActionListener(e -> findForCancel());
        JPanel search = new JPanel(new BorderLayout(10, 0));
        search.setOpaque(false);
        search.add(cancelId, BorderLayout.CENTER);
        search.add(find, BorderLayout.EAST);

        details.setFont(UIStyle.LABEL_FONT);
        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xFCA5A5)), new EmptyBorder(14, 16, 14, 16)));

        cancelBtn = UIStyle.dangerButton("Cancel Registration");
        cancelBtn.setForeground(Color.WHITE);
        // CHANGED: removed cancelBtn.setEnabled(false) so the text stays white.
        // The click handler (confirmAndCancel) already ignores clicks until a participant is found.
        cancelBtn.addActionListener(e -> confirmAndCancel());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btns.setOpaque(false);
        btns.add(cancelBtn);

        JPanel body = new JPanel(new GridLayout(0, 1, 0, 14));
        body.setOpaque(false);
        body.add(warn);
        body.add(UIStyle.labeled("Find registration", search, null));
        body.add(details);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(body, BorderLayout.NORTH);

        UIStyle.Card card = new UIStyle.Card(new BorderLayout(0, 12));
        card.add(wrap, BorderLayout.CENTER);
        card.add(btns, BorderLayout.SOUTH);
        return UIStyle.page(card);
    }

    private void findForCancel() {
        int id;
        try {
            id = Integer.parseInt(cancelId.getText().trim());
        } catch (NumberFormatException ex) {
            UIStyle.toast(this, "Participant ID must be a number", false);
            return;
        }
        try {
            cancelTarget = registrationDAO.getParticipantById(id);
            if (cancelTarget == null) {
                details.setText("No participant found with that ID");
                // CHANGED: removed cancelBtn.setEnabled(false)
                return;
            }
            String date = "-";
            cancelEventName = "Event #" + cancelTarget.getEventId();
            for (Event ev : eventDAO.getAllEvents()) {
                if (ev.getEventId() == cancelTarget.getEventId()) {
                    cancelEventName = ev.getEventName();
                    date = ev.getEventDate();
                }
            }
            details.setForeground(UIStyle.TEXT);
            details.setIcon(UIStyle.avatarIcon(cancelTarget.getParticipantName(), 44));
            details.setIconTextGap(14);
            details.setText("<html><b style='font-size:14px'>" + cancelTarget.getParticipantName() + "</b><br>"
                    + cancelEventName + "  ·  " + date + "<br>" + cancelTarget.getEmail() + "</html>");
            // CHANGED: removed cancelBtn.setEnabled(true)
        } catch (SQLException ex) {
            UIStyle.toast(this, "Database error: " + ex.getMessage(), false);
        }
    }

    private void confirmAndCancel() {
        // CHANGED: show a message instead of silently doing nothing
        if (cancelTarget == null) {
            UIStyle.toast(this, "Find a participant first", false);
            return;
        }
        int choice = JOptionPane.showOptionDialog(this,
                "Cancel registration for " + cancelTarget.getParticipantName() + " in " + cancelEventName
                        + "?\nThis cannot be undone.",
                "Confirm cancellation", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                new Object[]{"Keep", "Yes, cancel"}, "Keep");
        if (choice != 1) return;
        try {
            if (registrationDAO.cancelRegistration(cancelTarget.getParticipantId())) {
                UIStyle.toast(this, "Registration cancelled. 1 seat freed.", true);
                cancelId.setText("");
                cancelTarget = null;
                // CHANGED: removed cancelBtn.setEnabled(false)
                details.setIcon(null);
                details.setForeground(UIStyle.MUTED);
                details.setText("Find a participant to see their registration");
            } else {
                UIStyle.toast(this, "No participant found with that ID", false);
            }
        } catch (SQLException ex) {
            UIStyle.toast(this, "Database error: " + ex.getMessage(), false);
        }
    }
}