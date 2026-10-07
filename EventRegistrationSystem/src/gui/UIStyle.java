package gui;

import model.Participant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared look-and-feel helpers: palette, fonts, rounded buttons, cards,
 * seat bars, status pills, initials avatars and toast messages.
 * Old helper names are kept so existing panels still compile.
 */
public class UIStyle {

    // ---- Palette (Coral / Rose / Amber theme) ----
    public static final Color SIDEBAR = new Color(0x0F172A);
    public static final Color PRIMARY = new Color(0xE43D12);      // Coral Red
    public static final Color PRIMARY_DARK = new Color(0xC6330E);
    public static final Color SECONDARY = new Color(0xD6536D);    // Rose Pink
    public static final Color SOFT_PINK = new Color(0xFFA2B6);    // Soft Pink
    public static final Color ACCENT = new Color(0x10B981);       // Emerald Success
    public static final Color WARNING = new Color(0xF59E0B);      // Warm Gold
    public static final Color DANGER = new Color(0xDC2626);       // Red Danger
    public static final Color BACKGROUND = new Color(0xF8FAFC);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color BORDER = new Color(0xE2E8F0);

    // ---- Fonts ----
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public static void aa(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // ---- Buttons ----
    public static JButton primaryButton(String text) { return new RoundButton(text, PRIMARY, false); }
    public static JButton successButton(String text) { return new RoundButton(text, ACCENT, false); }
    public static JButton dangerButton(String text) { return new RoundButton(text, DANGER, false); }
    public static JButton secondaryButton(String text) { return new RoundButton(text, SECONDARY, true); }

    /** Rounded button with hover lighten. Filled = primary/success/danger, outline = secondary. */
    static class RoundButton extends JButton {
        private final Color base;
        private final boolean outline;
        private boolean hover;

        RoundButton(String text, Color base, boolean outline) {
            super(text);
            this.base = base;
            this.outline = outline;
            setFont(BUTTON_FONT);
            setForeground(outline ? base : Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(9, 18, 9, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            int w = getWidth(), h = getHeight();
            if (outline) {
                // Same size as the filled buttons; crisp 2px border (outer base-colour pill + inner white pill).
                g2.setColor(base);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.setColor(hover ? new Color(0xF1F5F9) : Color.WHITE);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 10, 10);
            } else {
                g2.setColor(hover ? blend(base, Color.WHITE, 0.15f) : base);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static Color blend(Color a, Color b, float t) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    // ---- Labels / fields (backward compatible) ----
    public static JLabel headerLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        label.setForeground(Color.WHITE);
        return label;
    }

    public static JPanel headerPanel(String title) {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        header.setBackground(PRIMARY);
        header.add(headerLabel(title));
        return header;
    }

    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(TEXT);
        return label;
    }

    public static void styleTextField(JTextField field) {
        field.setFont(LABEL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                new EmptyBorder(6, 10, 6, 10)));
    }

    /** Red border for inline validation; call styleTextField(field) to reset. */
    public static void markInvalid(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DANGER),
                new EmptyBorder(6, 10, 6, 10)));
    }

    // ---- Card: white rounded panel ----
    public static class Card extends JPanel {
        public Card(LayoutManager lm) {
            super(lm);
            setOpaque(false);
            setBorder(new EmptyBorder(20, 20, 20, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fillRoundRect(1, 3, getWidth() - 2, getHeight() - 3, 18, 18);
            g2.setColor(CARD);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 3, 18, 18);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 3, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---- Seat bar: green -> orange -> red as it fills ----
    public static Color seatColor(int used, int capacity) {
        if (capacity <= 0 || used >= capacity) return DANGER;
        double r = used / (double) capacity;
        if (r >= 0.75) return WARNING;
        return ACCENT;
    }

    public static class SeatBar extends JComponent {
        private int used, capacity;

        public SeatBar() { setPreferredSize(new Dimension(140, 8)); }

        public void set(int used, int capacity) {
            this.used = used;
            this.capacity = capacity;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            int h = 8, y = (getHeight() - h) / 2, w = getWidth();
            g2.setColor(new Color(0xE5E7EB));
            g2.fillRoundRect(0, y, w, h, h, h);
            if (capacity > 0) {
                int fw = (int) (w * Math.min(1.0, used / (double) capacity));
                g2.setColor(seatColor(used, capacity));
                g2.fillRoundRect(0, y, Math.max(fw, used > 0 ? h : 0), h, h, h);
            }
            g2.dispose();
        }
    }

    // ---- Status pill: Open / Almost Full / Full ----
    public static String statusText(int used, int capacity) {
        if (capacity > 0 && used >= capacity) return "Full";
        if (capacity > 0 && used / (double) capacity >= 0.75) return "Almost Full";
        return "Open";
    }

    public static class Pill extends JLabel {
        private Color color = ACCENT;

        public Pill(String text, Color color) {
            super(text, SwingConstants.CENTER);
            this.color = color;
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(color.darker());
            setBorder(new EmptyBorder(3, 12, 3, 12));
            setOpaque(false);
        }

        public static Pill forSeats(int used, int capacity) {
            return new Pill(statusText(used, capacity), seatColor(used, capacity));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 35));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---- Initials avatar ----
    private static final Color[] AVATAR_COLORS = {
            new Color(0x4F46E5), new Color(0x0EA5E9), new Color(0x22C55E),
            new Color(0xF59E0B), new Color(0xEC4899), new Color(0x8B5CF6)};

    public static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] p = name.trim().split("\\s+");
        String s = p.length == 1 ? p[0].substring(0, 1) : p[0].substring(0, 1) + p[p.length - 1].substring(0, 1);
        return s.toUpperCase();
    }

    public static class Avatar extends JComponent {
        private final String initials;
        private final Color color;

        public Avatar(String name, int size) {
            this.initials = initials(name);
            this.color = AVATAR_COLORS[Math.abs((name == null ? "" : name).hashCode()) % AVATAR_COLORS.length];
            setPreferredSize(new Dimension(size, size));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            int d = Math.min(getWidth(), getHeight());
            g2.setColor(color);
            g2.fillOval(0, 0, d, d);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, d / 3 + 2));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initials, (d - fm.stringWidth(initials)) / 2, (d + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    // ---- Toast: banner at top of the window, auto-hides after 3 s ----
    public static void toast(Component parent, String message, boolean success) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JWindow w = new JWindow(owner);
        JLabel l = new JLabel(message);
        l.setFont(BUTTON_FONT);
        l.setForeground(Color.WHITE);
        l.setOpaque(true);
        l.setBackground(success ? ACCENT.darker() : DANGER);
        l.setBorder(new EmptyBorder(12, 22, 12, 22));
        w.add(l);
        w.pack();
        if (owner != null) {
            w.setLocation(owner.getX() + (owner.getWidth() - w.getWidth()) / 2, owner.getY() + 80);
        } else {
            w.setLocationRelativeTo(null);
        }
        w.setAlwaysOnTop(true);
        w.setVisible(true);
        Timer t = new Timer(3000, e -> w.dispose());
        t.setRepeats(false);
        t.start();
    }

    // ---- Page header + form helpers ----
    public static JPanel pageHeader(String title, String subtitle) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PRIMARY);
        bar.setBorder(new EmptyBorder(14, 24, 14, 24));
        JLabel t = new JLabel(title);
        t.setFont(TITLE_FONT);
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel(subtitle);
        s.setFont(LABEL_FONT);
        s.setForeground(new Color(0xE2E8F0));
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(t);
        left.add(s);
        JLabel d = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")));
        d.setFont(BUTTON_FONT);
        d.setForeground(Color.WHITE);
        bar.add(left, BorderLayout.WEST);
        bar.add(d, BorderLayout.EAST);
        return bar;
    }

    /** Light-gray page area that holds one card. */
    public static JPanel page(JComponent card) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BACKGROUND);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    /** Label above field, small message line below (for inline validation). */
    public static JPanel labeled(String label, JComponent field, JLabel msg) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(fieldLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        if (msg != null) p.add(msg, BorderLayout.SOUTH);
        return p;
    }

    public static JLabel msgLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    /** blank -> hidden, ok -> green tick, otherwise red cross with message. */
    public static void setCheck(JLabel l, boolean blank, boolean ok, String bad) {
        if (blank) { l.setText(" "); return; }
        l.setForeground(ok ? ACCENT.darker() : DANGER);
        l.setText(ok ? "Looks good" : bad);
    }

    public static void onChange(JTextComponent c, Runnable r) {
        c.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        });
    }

    /** Text field with grey placeholder text. */
    public static class Field extends JTextField {
        private final String hint;

        public Field(String hint, int cols) {
            super(cols);
            this.hint = hint;
            styleTextField(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !hint.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                aa(g2);
                g2.setColor(new Color(0x9CA3AF));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        }
    }

    // ---- Table styling: zebra rows, dark header, no grid, hover highlight ----
    static class ZebraRenderer extends DefaultTableCellRenderer {
        private final int[] hover;
        private final boolean status, avatar;

        ZebraRenderer(int[] hover, boolean status, boolean avatar) {
            this.hover = hover;
            this.status = status;
            this.avatar = avatar;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, row, col);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(LABEL_FONT);
            setForeground(TEXT);
            setIcon(null);
            setBackground(sel ? new Color(0xE2E8F0) : row == hover[0] ? new Color(0xF1F5F9)
                    : row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
            if (status && v != null) {
                String s = v.toString();
                setForeground("Full".equals(s) ? DANGER : "Almost Full".equals(s) ? WARNING.darker() : ACCENT.darker());
                setFont(BUTTON_FONT);
            }
            if (avatar && v != null) {
                setIcon(avatarIcon(v.toString(), 26));
                setIconTextGap(10);
            }
            return this;
        }
    }

    public static void styleTable(JTable t) {
        t.setRowHeight(36);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        int[] hover = {-1};
        t.putClientProperty("hover", hover);
        t.setDefaultRenderer(Object.class, new ZebraRenderer(hover, false, false));
        t.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { hover[0] = t.rowAtPoint(e.getPoint()); t.repaint(); }
        });
        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hover[0] = -1; t.repaint(); }
        });
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(tb, v, false, false, r, c);
                setBackground(SIDEBAR);
                setForeground(Color.WHITE);
                setFont(BUTTON_FONT);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    /** Give one column the status-colour and/or initials-avatar look. */
    public static void decorateColumn(JTable t, int col, boolean status, boolean avatar) {
        int[] hover = (int[]) t.getClientProperty("hover");
        t.getColumnModel().getColumn(col).setCellRenderer(new ZebraRenderer(hover, status, avatar));
    }

    public static Icon avatarIcon(String name, int size) {
        Color c = AVATAR_COLORS[Math.abs((name == null ? "" : name).hashCode()) % AVATAR_COLORS.length];
        String in = initials(name);
        return new Icon() {
            public void paintIcon(Component cmp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                aa(g2);
                g2.setColor(c);
                g2.fillOval(x, y, size, size);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, size / 3 + 1));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(in, x + (size - fm.stringWidth(in)) / 2, y + (size + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }
        };
    }

    // ---- Data helpers ----
    public static LocalDate parseDate(String s) {
        if (s == null) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /** eventId -> number of registered participants. Assumes Participant.getEventId(). */
    public static Map<Integer, Integer> countByEvent(List<Participant> parts) {
        Map<Integer, Integer> m = new HashMap<>();
        for (Participant p : parts) m.merge(p.getEventId(), 1, Integer::sum);
        return m;
    }
}