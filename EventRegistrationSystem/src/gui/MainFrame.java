package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Main window: dark charcoal sidebar, deep slate header strip, embedded panel switching via CardLayout.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final DashboardPanel dashboard;
    private final EventPanel eventPanel;
    private final ParticipantPanel participantPanel;
    private final RegistrationPanel registrationPanel;

    private final List<NavButton> navButtons = new ArrayList<>();
    private JLabel headerTitleLabel;
    private JLabel headerSubtitleLabel;

    public MainFrame() {
        setTitle("Event Registration System - EventHub");
        setSize(1180, 720);
        setMinimumSize(new Dimension(980, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.BACKGROUND);

        dashboard = new DashboardPanel(
                () -> showPage("EVENT", 0),
                () -> showPage("PARTICIPANT", 0));
        eventPanel = new EventPanel(0);
        participantPanel = new ParticipantPanel(0);
        registrationPanel = new RegistrationPanel(0);

        cardPanel.setOpaque(false);
        cardPanel.add(dashboard, "DASHBOARD");
        cardPanel.add(eventPanel, "EVENT");
        cardPanel.add(participantPanel, "PARTICIPANT");
        cardPanel.add(registrationPanel, "REGISTRATION");

        add(sidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIStyle.BACKGROUND);
        main.add(pageHeader("EventHub – Event Central", "Plan Events. Connect People. Create Memories."), BorderLayout.NORTH);
        main.add(cardPanel, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        showPage("DASHBOARD", 0);
    }

    private void setActiveNav(int index) {
        for (int i = 0; i < navButtons.size(); i++) {
            navButtons.get(i).setActive(i == index);
        }
    }

    private void updateHeader(String title, String subtitle) {
        if (headerTitleLabel != null) headerTitleLabel.setText(title);
        if (headerSubtitleLabel != null) headerSubtitleLabel.setText(subtitle);
    }

    private void showPage(String cardKey, int tabIndex) {
        if ("DASHBOARD".equals(cardKey)) {
            setActiveNav(0);
            updateHeader("EventHub – Event Central", "Plan Events. Connect People. Create Memories.");
            dashboard.refresh();
            cardLayout.show(cardPanel, "DASHBOARD");
        } else if ("EVENT".equals(cardKey)) {
            setActiveNav(tabIndex + 1); // 1: Create, 2: View, 3: Search
            updateHeader("Event Management", "Create, browse and search your events");
            eventPanel.selectTab(tabIndex);
            eventPanel.loadData();
            cardLayout.show(cardPanel, "EVENT");
        } else if ("PARTICIPANT".equals(cardKey)) {
            setActiveNav(tabIndex == 0 ? 4 : 5); // 4: Register, 5: View
            updateHeader("Participant Management", "Register and view event participants");
            participantPanel.selectTab(tabIndex);
            participantPanel.loadData();
            cardLayout.show(cardPanel, "PARTICIPANT");
        } else if ("REGISTRATION".equals(cardKey)) {
            setActiveNav(tabIndex == 0 ? 6 : 7); // 6: Modify, 7: Cancel
            updateHeader("Registration Management", "Update details or cancel a registration");
            registrationPanel.selectTab(tabIndex);
            cardLayout.show(cardPanel, "REGISTRATION");
        }
    }

    // ---- Page header strip: title, subtitle, today's date ----
    private JPanel pageHeader(String title, String subtitle) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x1E293B));
        bar.setBorder(new EmptyBorder(16, 24, 16, 24));

        headerTitleLabel = new JLabel(title);
        headerTitleLabel.setFont(UIStyle.TITLE_FONT);
        headerTitleLabel.setForeground(Color.WHITE);
        headerSubtitleLabel = new JLabel(subtitle);
        headerSubtitleLabel.setFont(UIStyle.LABEL_FONT);
        headerSubtitleLabel.setForeground(new Color(0xE2E8F0));
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(headerTitleLabel);
        left.add(headerSubtitleLabel);

        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")));
        date.setFont(UIStyle.BUTTON_FONT);
        date.setForeground(Color.WHITE);

        bar.add(left, BorderLayout.WEST);
        bar.add(date, BorderLayout.EAST);
        return bar;
    }

    // ---- Sidebar ----
    private JPanel sidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(UIStyle.SIDEBAR);
        side.setPreferredSize(new Dimension(230, 0));
        side.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel brand = new JLabel("  EventHub");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(Color.WHITE);
        brand.setBorder(new EmptyBorder(0, 14, 24, 0));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(brand);

        navButtons.clear();
        side.add(addNav("Dashboard", true, () -> showPage("DASHBOARD", 0)));
        side.add(Box.createVerticalStrut(6));
        side.add(addNav("Create Event", false, () -> showPage("EVENT", 0)));
        side.add(Box.createVerticalStrut(6));
        side.add(addNav("View Events", false, () -> showPage("EVENT", 1)));
        side.add(Box.createVerticalStrut(6));
        side.add(addNav("Search Events", false, () -> showPage("EVENT", 2)));
        side.add(Box.createVerticalStrut(6));
        side.add(addNav("Register Participant", false, () -> showPage("PARTICIPANT", 0)));
        side.add(Box.createVerticalStrut(6));
        side.add(addNav("View Participants", false, () -> showPage("PARTICIPANT", 1)));
        side.add(Box.createVerticalStrut(6));
        side.add(addNav("Modify Registration", false, () -> showPage("REGISTRATION", 0)));
        side.add(Box.createVerticalStrut(6));
        side.add(addNav("Cancel Registration", false, () -> showPage("REGISTRATION", 1)));
        side.add(Box.createVerticalGlue());
        side.add(addNav("Exit", false, () -> System.exit(0)));
        return side;
    }

    private JButton addNav(String text, boolean active, Runnable action) {
        NavButton b = new NavButton(text, active);
        b.addActionListener(e -> action.run());
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        navButtons.add(b);
        return b;
    }

    /** Sidebar item: hover tint, and a Coral Red left bar when active. */
    private static class NavButton extends JButton {
        private boolean active;
        private boolean hover;

        NavButton(String text, boolean active) {
            super(text);
            this.active = active;
            setFont(new Font("Segoe UI", Font.PLAIN, 15));
            setForeground(active ? Color.WHITE : new Color(0x94A3B8));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, 24, 0, 10));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e) { hover = false; repaint(); }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Color.WHITE : new Color(0x94A3B8));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (active || hover) {
                g2.setColor(active ? new Color(255, 255, 255, 24) : new Color(255, 255, 255, 12));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            if (active) {
                g2.setColor(UIStyle.PRIMARY); // Coral Red (#E43D12) indicator
                g2.fillRect(0, 0, 4, getHeight());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}