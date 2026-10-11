package ke.sonclinic;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.function.BiConsumer;

public final class Ui {
    private static final String NAVY = "#10243A";
    private static final String BLUE = "#176B87";
    private static final String BG = "#F3F6FA";
    private static final String CSS = """
        .root { -fx-font-family: 'Segoe UI', Arial; -fx-background-color: #F3F6FA; }
        .label { -fx-text-fill: #203247; }
        .title { -fx-font-size: 24px; -fx-font-weight: bold; }
        .subtitle { -fx-font-size: 12px; -fx-text-fill: #718096; }
        .button { -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 9 14; }
        .primary { -fx-background-color: #176B87; -fx-text-fill: white; -fx-font-weight: bold; }
        .navbutton { -fx-background-color: transparent; -fx-text-fill: #EAF3FA; -fx-alignment: CENTER_LEFT; -fx-padding: 12 15; -fx-font-size: 13px; }
        .navbutton:hover { -fx-background-color: #24415D; }
        .card { -fx-background-color: white; -fx-background-radius: 13; -fx-padding: 18; -fx-effect: dropshadow(gaussian, #14273A12, 12, 0.15, 0, 3); }
        .text-field, .password-field, .combo-box, .date-picker, .text-area { -fx-background-radius: 7; -fx-border-radius: 7; -fx-border-color: #D9E2EC; -fx-background-color: white; -fx-padding: 8; }
        .table-view { -fx-background-radius: 8; -fx-border-color: #E0E7EF; -fx-border-radius: 8; }
        .table-view .column-header-background { -fx-background-color: #EAF0F6; }
        .table-view .column-header .label { -fx-font-weight: bold; -fx-text-fill: #294057; }
        .danger { -fx-background-color: #FCE8E8; -fx-text-fill: #A51D2D; }
        """;

    private Ui() {}

    public static void error(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }
    private static void info(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }
    private static boolean confirm(String title, String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        a.setTitle(title); a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }
    private static Label label(String text) { return new Label(text); }
    private static Label heading(String text) { Label l = label(text); l.getStyleClass().add("title"); return l; }
    private static Button button(String text) { return new Button(text); }
    private static void style(Scene scene) { scene.getStylesheets().add("data:text/css," + CSS.replace("\n", "").replace(" ", "%20").replace("#", "%23").replace("{", "%7B").replace("}", "%7D").replace(";", "%3B").replace(":", "%3A").replace(",", "%2C").replace("'", "%27")); }

    public static Scene loginScene(BiConsumer<String,String> onLogin) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0D2035, #176B87);");
        VBox card = new VBox(14);
        card.setMaxWidth(390);
        card.setPadding(new Insets(34));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; -fx-effect: dropshadow(gaussian, #00000040, 28, .2, 0, 8);");
        Label brand = new Label("SON");
        brand.setFont(Font.font("System", 34)); brand.setTextFill(Color.web(BLUE)); brand.setStyle("-fx-font-weight: 900;");
        Label title = new Label("Clinic & Pharmacy");
        title.setFont(Font.font(21)); title.setStyle("-fx-font-weight: bold;");
        Label sub = new Label("Management System"); sub.setStyle("-fx-text-fill: #728096;");
        TextField user = new TextField("admin"); user.setPromptText("Username");
        PasswordField pass = new PasswordField(); pass.setPromptText("Password");
        Button login = button("Sign in"); login.getStyleClass().add("primary"); login.setMaxWidth(Double.MAX_VALUE);
        Label note = new Label("Demo login: admin / ChangeMe123!");
        note.setStyle("-fx-text-fill: #738296; -fx-font-size: 11px;");
        Runnable action = () -> onLogin.accept(user.getText(), pass.getText());
        login.setOnAction(e -> action.run()); pass.setOnAction(e -> action.run());
        card.getChildren().addAll(brand, title, sub, new Separator(), label("USERNAME"), user, label("PASSWORD"), pass, login, note);
        root.setCenter(card);
        Scene scene = new Scene(root, 1050, 700);
        return scene;
    }

    public static Scene mainScene(String username, Runnable onLogout) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + ";");
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(22, 12, 18, 12));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: " + NAVY + ";");
        Label logo = new Label("SON"); logo.setTextFill(Color.WHITE); logo.setFont(Font.font("System", 30)); logo.setStyle("-fx-font-weight: 900;");
        Label brand = new Label("CLINIC + PHARMACY"); brand.setTextFill(Color.web("#AFC8DB")); brand.setStyle("-fx-font-size: 10px; -fx-letter-spacing: 1px;");
        VBox logoBox = new VBox(3, logo, brand); logoBox.setPadding(new Insets(0, 8, 22, 8));
        sidebar.getChildren().addAll(logoBox, new Separator());
        StackPane content = new StackPane();
        content.setPadding(new Insets(24));
        Runnable dashboard = () -> content.getChildren().setAll(dashboardView(username));
        Button dash = nav("▦   Dashboard", () -> dashboard.run());
        Button patients = nav("♙   Patients", () -> content.getChildren().setAll(patientView()));
        Button appointments = nav("◷   Appointments", () -> content.getChildren().setAll(appointmentView()));
        Button inventory = nav("▤   Pharmacy inventory", () -> content.getChildren().setAll(inventoryView()));
        Button sales = nav("₵   Record sale", () -> content.getChildren().setAll(salesView()));
        Button settings = nav("⚙   Account security", () -> content.getChildren().setAll(securityView(username)));
        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        Button logout = nav("↪   Sign out", onLogout);
        sidebar.getChildren().addAll(dash, patients, appointments, inventory, sales, settings, spacer, logout);
        root.setLeft(sidebar);
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_RIGHT); top.setPadding(new Insets(14, 26, 14, 26));
        top.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #E2E8F0 transparent;");
        Label logged = new Label("Signed in as " + username + "   •   SON Clinic");
        logged.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
        top.getChildren().add(logged); root.setTop(top);
        root.setCenter(content);
        dashboard.run();
        Scene scene = new Scene(root, 1200, 760);
        style(scene);
        return scene;
    }

    private static Button nav(String text, Runnable action) {
        Button b = button(text); b.getStyleClass().add("navbutton"); b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> action.run()); return b;
    }
    private static VBox page(String title, String description, Node... children) {
        VBox box = new VBox(16); box.setFillWidth(true);
        HBox titleBox = new HBox(12); titleBox.setAlignment(Pos.CENTER_LEFT);
        VBox text = new VBox(4, heading(title), label(description)); text.getChildren().get(1).getStyleClass().add("subtitle");
        titleBox.getChildren().add(text); box.getChildren().add(titleBox); box.getChildren().addAll(children);
        return box;
    }
    private static VBox card(Node... nodes) {
        VBox v = new VBox(10, nodes); v.getStyleClass().add("card"); return v;
    }
    private static Label statValue(String value) {
        Label l = label(value); l.setFont(Font.font("System", 28)); l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + BLUE + ";"); return l;
    }

    private static Node dashboardView(String username) {
        VBox page = page("Dashboard", "A quick overview of today's clinic and pharmacy operations.");
        HBox stats = new HBox(14);
        stats.getChildren().addAll(
            statCard("REGISTERED PATIENTS", String.valueOf(Database.count("patients")), "Patient records"),
            statCard("APPOINTMENTS", String.valueOf(Database.count("appointments")), "All scheduled visits"),
            statCard("MEDICINES", String.valueOf(Database.count("medicines")), "Inventory items"),
            statCard("TODAY'S SALES", String.format("KES %,.2f", Database.todaySales()), "Recorded sales")
        );
        stats.setAlignment(Pos.TOP_LEFT);
        Label welcome = heading("Welcome back, " + username);
        Label msg = label("Use the navigation menu to register patients, schedule appointments, manage medicine stock, and record sales.");
        msg.setWrapText(true);
        VBox quick = card(welcome, msg);
        Label caution = label("Important: This starter system is for demonstration and learning. Verify clinical and pharmacy workflows before any real-world use.");
        caution.setWrapText(true); caution.setStyle("-fx-text-fill: #8A5A00; -fx-background-color: #FFF4D6; -fx-padding: 12; -fx-background-radius: 8;");
        page.getChildren().addAll(stats, quick, caution);
        ScrollPane sc = new ScrollPane(page); sc.setFitToWidth(true); sc.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return sc;
    }
    private static VBox statCard(String title, String value, String sub) {
        VBox v = card(label(title), statValue(value), label(sub)); v.setMinWidth(150); HBox.setHgrow(v, Priority.ALWAYS);
        v.getChildren().get(0).setStyle("-fx-text-fill: #718096; -fx-font-size: 10px; -fx-font-weight: bold;");
        v.getChildren().get(2).getStyleClass().add("subtitle"); return v;
    }

    private static Node patientView() {
        TextField name = new TextField(); name.setPromptText("Full name *");
        TextField phone = new TextField(); phone.setPromptText("Phone number");
        ComboBox<String> gender = new ComboBox<>(FXCollections.observableArrayList("Female","Male","Other","Prefer not to say")); gender.setPromptText("Gender");
        DatePicker dob = new DatePicker(); dob.setPromptText("Date of birth");
        TextArea notes = new TextArea(); notes.setPromptText("Notes (optional)"); notes.setPrefRowCount(2);
        Button add = button("Register patient"); add.getStyleClass().add("primary");
        TextField search = new TextField(); search.setPromptText("Search by patient number, name or phone");
        TableView<PatientRow> table = new TableView<>();
        table.getColumns().addAll(col("Patient No","patientNo",150), col("Full name","name",220), col("Phone","phone",140), col("Gender","gender",110), col("Date of birth","dob",130));
        Runnable refresh = () -> loadPatients(table, search.getText());
        search.textProperty().addListener((o,a,b)->refresh.run());
        add.setOnAction(e -> {
            if(name.getText().isBlank()){error("Missing name","Enter the patient's full name.");return;}
            String no = "P-" + System.currentTimeMillis();
            try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("INSERT INTO patients(patient_no,full_name,phone,gender,date_of_birth,notes) VALUES(?,?,?,?,?,?)")){
                p.setString(1,no);p.setString(2,name.getText().trim());p.setString(3,phone.getText().trim());p.setString(4,gender.getValue());
                p.setString(5,dob.getValue()==null?null:dob.getValue().toString());p.setString(6,notes.getText().trim());p.executeUpdate();
                name.clear();phone.clear();gender.setValue(null);dob.setValue(null);notes.clear();refresh.run();info("Saved","Patient registered: "+no);
            }catch(SQLException ex){error("Could not save patient",ex.getMessage());}
        });
        HBox row1=new HBox(10,name,phone,gender,dob); row1.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(name,Priority.ALWAYS);HBox.setHgrow(phone,Priority.ALWAYS);
        VBox form=card(label("Register a patient"),row1,notes,add);
        VBox list=card(new HBox(12,heading("Patient records"),new Region()),search,table); VBox.setVgrow(table,Priority.ALWAYS);
        VBox content=page("Patients","Register and find patient records.",form,list); VBox.setVgrow(list,Priority.ALWAYS); VBox.setVgrow(table,Priority.ALWAYS);
        refresh.run(); return content;
    }
    private static void loadPatients(TableView<PatientRow> table, String query) {
        ObservableList<PatientRow> data=FXCollections.observableArrayList();
        String q="%"+(query==null?"":query.trim())+"%";
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("SELECT patient_no,full_name,phone,gender,date_of_birth FROM patients WHERE patient_no LIKE ? OR full_name LIKE ? OR phone LIKE ? ORDER BY id DESC")){
            p.setString(1,q);p.setString(2,q);p.setString(3,q);
            try(ResultSet rs=p.executeQuery()){while(rs.next())data.add(new PatientRow(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)));}
        }catch(SQLException e){error("Database error",e.getMessage());}
        table.setItems(data);
    }
    public static class PatientRow {
        private final String patientNo,name,phone,gender,dob;
        PatientRow(String a,String b,String c,String d,String e){patientNo=a;name=b;phone=c;gender=d;dob=e;}
        public String getPatientNo(){return patientNo;} public String getName(){return name;} public String getPhone(){return phone;} public String getGender(){return gender;} public String getDob(){return dob;}
    }

    private static Node appointmentView() {
        ComboBox<PatientChoice> patient=new ComboBox<>(); patient.setPromptText("Select patient"); patient.setPrefWidth(260); loadPatientChoices(patient);
        DatePicker date=new DatePicker(LocalDate.now()); TextField reason=new TextField();reason.setPromptText("Reason / department");
        Button add=button("Schedule appointment");add.getStyleClass().add("primary");
        TableView<AppointmentRow> table=new TableView<>();
        table.getColumns().addAll(col("ID","id",60),col("Patient","patient",220),col("Date","date",140),col("Reason","reason",240),col("Status","status",120));
        Runnable refresh=()->loadAppointments(table);
        add.setOnAction(e->{if(patient.getValue()==null||date.getValue()==null){error("Missing details","Select a patient and appointment date.");return;}
            try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("INSERT INTO appointments(patient_id,appointment_date,reason,status) VALUES(?,?,?,'SCHEDULED')")){
                p.setInt(1,patient.getValue().id);p.setString(2,date.getValue().toString());p.setString(3,reason.getText().trim());p.executeUpdate();reason.clear();refresh.run();info("Saved","Appointment scheduled.");
            }catch(SQLException ex){error("Could not schedule",ex.getMessage());}
        });
        HBox fields=new HBox(10,patient,date,reason,add);fields.setAlignment(Pos.CENTER_LEFT);HBox.setHgrow(reason,Priority.ALWAYS);
        VBox form=card(label("Schedule a visit"),fields);
        Button complete=button("Mark completed");complete.getStyleClass().add("primary");Button cancel=button("Cancel appointment");cancel.getStyleClass().add("danger");
        complete.setOnAction(e->updateAppointment(table,"COMPLETED",refresh));cancel.setOnAction(e->updateAppointment(table,"CANCELLED",refresh));
        HBox actions=new HBox(10,complete,cancel);
        VBox list=card(heading("Appointments"),table,actions);VBox content=page("Appointments","Plan and track patient visits.",form,list);VBox.setVgrow(table,Priority.ALWAYS);VBox.setVgrow(list,Priority.ALWAYS);
        refresh.run();return content;
    }
    private static void loadPatientChoices(ComboBox<PatientChoice> box){
        ObservableList<PatientChoice> list=FXCollections.observableArrayList();
        try(Connection c=Database.connect();Statement s=c.createStatement();ResultSet rs=s.executeQuery("SELECT id,patient_no,full_name FROM patients ORDER BY full_name")){
            while(rs.next())list.add(new PatientChoice(rs.getInt(1),rs.getString(2),rs.getString(3)));
        }catch(SQLException e){error("Database error",e.getMessage());}
        box.setItems(list);
    }
    private static void loadAppointments(TableView<AppointmentRow> table){
        ObservableList<AppointmentRow> list=FXCollections.observableArrayList();
        String sql="SELECT a.id,p.full_name,a.appointment_date,a.reason,a.status FROM appointments a JOIN patients p ON p.id=a.patient_id ORDER BY a.appointment_date DESC,a.id DESC";
        try(Connection c=Database.connect();Statement s=c.createStatement();ResultSet rs=s.executeQuery(sql)){while(rs.next())list.add(new AppointmentRow(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)));}
        catch(SQLException e){error("Database error",e.getMessage());}table.setItems(list);
    }
    private static void updateAppointment(TableView<AppointmentRow> table,String status,Runnable refresh){
        AppointmentRow row=table.getSelectionModel().getSelectedItem();if(row==null){error("Select appointment","Select an appointment from the table first.");return;}
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("UPDATE appointments SET status=? WHERE id=?")){
            p.setString(1,status);p.setInt(2,row.getId());p.executeUpdate();refresh.run();
        }catch(SQLException e){error("Update failed",e.getMessage());}
    }
    public static class PatientChoice {
        final int id;final String no,name;PatientChoice(int id,String no,String name){this.id=id;this.no=no;this.name=name;}
        @Override public String toString(){return no+" — "+name;}
    }
    public static class AppointmentRow {
        private final Integer id;private final String patient,date,reason,status;
        AppointmentRow(int i,String p,String d,String r,String s){id=i;patient=p;date=d;reason=r;status=s;}
        public Integer getId(){return id;}public String getPatient(){return patient;}public String getDate(){return date;}public String getReason(){return reason;}public String getStatus(){return status;}
    }

    private static Node inventoryView() {
        TextField name=new TextField();name.setPromptText("Medicine name *");TextField category=new TextField();category.setPromptText("Category");
        Spinner<Integer> qty=new Spinner<>(0,100000,0);qty.setEditable(true);qty.setPrefWidth(95);
        TextField price=new TextField("0.00");price.setPromptText("Unit price (KES)");
        DatePicker expiry=new DatePicker();expiry.setPromptText("Expiry date");
        Spinner<Integer> reorder=new Spinner<>(0,100000,10);reorder.setEditable(true);reorder.setPrefWidth(95);
        Button add=button("Add medicine");add.getStyleClass().add("primary");
        TextField search=new TextField();search.setPromptText("Search medicine or category");
        TableView<MedicineRow> table=new TableView<>();
        table.getColumns().addAll(col("ID","id",55),col("Medicine","name",200),col("Category","category",130),col("Stock","quantity",75),col("Unit price (KES)","price",125),col("Expiry","expiry",120),col("Stock status","status",130));
        Runnable refresh=()->loadMedicines(table,search.getText());search.textProperty().addListener((o,a,b)->refresh.run());
        add.setOnAction(e->{try{
            if(name.getText().isBlank())throw new IllegalArgumentException("Enter a medicine name.");
            int q=Integer.parseInt(qty.getEditor().getText().trim());int r=Integer.parseInt(reorder.getEditor().getText().trim());double p=Double.parseDouble(price.getText().trim());
            if(q<0||r<0||p<0||!Double.isFinite(p))throw new IllegalArgumentException("Quantity and price must be valid non-negative numbers.");
            try(Connection c=Database.connect();PreparedStatement ps=c.prepareStatement("INSERT INTO medicines(medicine_name,category,quantity,unit_price,expiry_date,reorder_level) VALUES(?,?,?,?,?,?)")){
                ps.setString(1,name.getText().trim());ps.setString(2,category.getText().trim());ps.setInt(3,q);ps.setDouble(4,p);ps.setString(5,expiry.getValue()==null?null:expiry.getValue().toString());ps.setInt(6,r);ps.executeUpdate();
            }
            name.clear();category.clear();qty.getValueFactory().setValue(0);price.setText("0.00");expiry.setValue(null);reorder.getValueFactory().setValue(10);refresh.run();info("Saved","Medicine added to inventory.");
        }catch(Exception ex){error("Could not add medicine",ex.getMessage());}});
        HBox fields=new HBox(8,name,category,qty,price,expiry,reorder,add);fields.setAlignment(Pos.CENTER_LEFT);HBox.setHgrow(name,Priority.ALWAYS);HBox.setHgrow(category,Priority.ALWAYS);
        VBox form=card(label("Add inventory item"),fields,label("Enter purchase/retail unit price in Kenyan shillings (KES)."));
        Button adjust=button("Adjust stock");adjust.getStyleClass().add("primary");
        Button delete=button("Delete selected");delete.getStyleClass().add("danger");
        adjust.setOnAction(e->{MedicineRow row=table.getSelectionModel().getSelectedItem();if(row==null){error("Select medicine","Select a medicine first.");return;}TextInputDialog d=new TextInputDialog("10");d.setTitle("Adjust stock");d.setHeaderText("Add units to "+row.getName());d.setContentText("Units to add:");d.showAndWait().ifPresent(v->{try{int n=Integer.parseInt(v);if(n<=0)throw new IllegalArgumentException("Enter a positive number.");try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("UPDATE medicines SET quantity=quantity+? WHERE id=?")){p.setInt(1,n);p.setInt(2,row.getId());p.executeUpdate();}refresh.run();}catch(Exception ex){error("Stock adjustment failed",ex.getMessage());}});});
        delete.setOnAction(e->{MedicineRow row=table.getSelectionModel().getSelectedItem();if(row==null){error("Select medicine","Select a medicine first.");return;}if(!confirm("Delete medicine","Delete "+row.getName()+"? Medicines with sales records cannot be deleted."))return;try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("DELETE FROM medicines WHERE id=?")){p.setInt(1,row.getId());p.executeUpdate();refresh.run();}catch(SQLException ex){error("Delete failed","This medicine may already have sales records. "+ex.getMessage());}});
        VBox list=card(new HBox(10,heading("Medicine inventory")),search,table,new HBox(10,adjust,delete));VBox content=page("Pharmacy Inventory","Monitor stock, prices, expiry dates, and reorder levels.",form,list);VBox.setVgrow(table,Priority.ALWAYS);VBox.setVgrow(list,Priority.ALWAYS);refresh.run();return content;
    }
    private static void loadMedicines(TableView<MedicineRow> table,String query){
        ObservableList<MedicineRow> list=FXCollections.observableArrayList();String q="%"+(query==null?"":query.trim())+"%";
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("SELECT id,medicine_name,category,quantity,unit_price,expiry_date,reorder_level FROM medicines WHERE medicine_name LIKE ? OR category LIKE ? ORDER BY medicine_name")){
            p.setString(1,q);p.setString(2,q);try(ResultSet rs=p.executeQuery()){while(rs.next()){int qty=rs.getInt(4),reorder=rs.getInt(7);String exp=rs.getString(6);String status=qty<=reorder?"LOW STOCK":"OK";if(exp!=null&&!exp.isBlank()&&exp.compareTo(LocalDate.now().toString())<0)status="EXPIRED";list.add(new MedicineRow(rs.getInt(1),rs.getString(2),rs.getString(3),qty,rs.getDouble(5),exp,status));}}
        }catch(SQLException e){error("Database error",e.getMessage());}table.setItems(list);
    }
    public static class MedicineRow{
        private final Integer id,quantity;private final String name,category,expiry,status;private final Double price;
        MedicineRow(int i,String n,String c,int q,double p,String e,String s){id=i;name=n;category=c;quantity=q;price=p;expiry=e;status=s;}
        public Integer getId(){return id;}public String getName(){return name;}public String getCategory(){return category;}public Integer getQuantity(){return quantity;}public Double getPrice(){return price;}public String getExpiry(){return expiry;}public String getStatus(){return status;}
    }

    private static Node salesView() {
        ComboBox<MedicineChoice> medicine=new ComboBox<>();medicine.setPromptText("Select medicine");medicine.setPrefWidth(300);loadMedicineChoices(medicine);
        Spinner<Integer> quantity=new Spinner<>(1,10000,1);quantity.setEditable(true);
        Label unit=new Label("Unit price: —");Label total=heading("Total: KES 0.00");
        medicine.setOnAction(e->updateSaleTotal(medicine,quantity,unit,total));quantity.valueProperty().addListener((o,a,b)->updateSaleTotal(medicine,quantity,unit,total));
        Button sell=button("Record sale");sell.getStyleClass().add("primary");
        TableView<SaleRow> table=new TableView<>();table.getColumns().addAll(col("Sale ID","id",80),col("Medicine","medicine",220),col("Quantity","quantity",100),col("Unit price (KES)","unitPrice",150),col("Total (KES)","total",150),col("Date","date",190));
        Runnable refresh=()->loadSales(table);
        sell.setOnAction(e->{MedicineChoice m=medicine.getValue();if(m==null){error("Select medicine","Choose a medicine first.");return;}try{
            int qty=Integer.parseInt(quantity.getEditor().getText().trim());if(qty<=0)throw new IllegalArgumentException("Quantity must be positive.");
            try(Connection c=Database.connect()){
                c.setAutoCommit(false);
                try(PreparedStatement check=c.prepareStatement("SELECT quantity,unit_price FROM medicines WHERE id=?")){check.setInt(1,m.id);try(ResultSet rs=check.executeQuery()){
                    if(!rs.next())throw new IllegalArgumentException("Medicine no longer exists.");int stock=rs.getInt(1);double price=rs.getDouble(2);if(qty>stock)throw new IllegalArgumentException("Not enough stock. Available: "+stock);
                    try(PreparedStatement upd=c.prepareStatement("UPDATE medicines SET quantity=quantity-? WHERE id=?")){upd.setInt(1,qty);upd.setInt(2,m.id);upd.executeUpdate();}
                    try(PreparedStatement ins=c.prepareStatement("INSERT INTO sales(medicine_id,quantity,unit_price,total) VALUES(?,?,?,?)")){ins.setInt(1,m.id);ins.setInt(2,qty);ins.setDouble(3,price);ins.setDouble(4,price*qty);ins.executeUpdate();}
                }}c.commit();
            }
            loadMedicineChoices(medicine);quantity.getValueFactory().setValue(1);updateSaleTotal(medicine,quantity,unit,total);refresh.run();info("Sale recorded","Sale saved and inventory reduced.");
        }catch(Exception ex){error("Could not record sale",ex.getMessage());}});
        VBox form=card(label("Record a pharmacy sale"),medicine,new HBox(12,label("Quantity"),quantity),unit,total,sell);
        VBox list=card(heading("Recent sales"),table);VBox content=page("Pharmacy Sales","Record sales and automatically deduct medicine stock.",form,list);VBox.setVgrow(table,Priority.ALWAYS);VBox.setVgrow(list,Priority.ALWAYS);refresh.run();return content;
    }
    private static void updateSaleTotal(ComboBox<MedicineChoice> medicine,Spinner<Integer> quantity,Label unit,Label total){
        MedicineChoice m=medicine.getValue();if(m==null){unit.setText("Unit price: —");total.setText("Total: KES 0.00");return;}
        unit.setText(String.format("Unit price: KES %,.2f  •  Available stock: %d",m.price,m.stock));
        int q=quantity.getValue()==null?1:quantity.getValue();total.setText(String.format("Total: KES %,.2f",m.price*q));
    }
    private static void loadMedicineChoices(ComboBox<MedicineChoice> box){
        ObservableList<MedicineChoice> list=FXCollections.observableArrayList();
        try(Connection c=Database.connect();Statement s=c.createStatement();ResultSet rs=s.executeQuery("SELECT id,medicine_name,quantity,unit_price,expiry_date FROM medicines ORDER BY medicine_name")){
            while(rs.next()){String exp=rs.getString(5);if(exp!=null&&!exp.isBlank()&&exp.compareTo(LocalDate.now().toString())<0)continue;if(rs.getInt(3)>0)list.add(new MedicineChoice(rs.getInt(1),rs.getString(2),rs.getInt(3),rs.getDouble(4)));}
        }catch(SQLException e){error("Database error",e.getMessage());}box.setItems(list);
    }
    private static void loadSales(TableView<SaleRow> table){
        ObservableList<SaleRow> list=FXCollections.observableArrayList();String sql="SELECT s.id,m.medicine_name,s.quantity,s.unit_price,s.total,s.sold_at FROM sales s JOIN medicines m ON m.id=s.medicine_id ORDER BY s.id DESC LIMIT 200";
        try(Connection c=Database.connect();Statement st=c.createStatement();ResultSet rs=st.executeQuery(sql)){while(rs.next())list.add(new SaleRow(rs.getInt(1),rs.getString(2),rs.getInt(3),rs.getDouble(4),rs.getDouble(5),rs.getString(6)));}
        catch(SQLException e){error("Database error",e.getMessage());}table.setItems(list);
    }
    public static class MedicineChoice {
        final int id,stock;final String name;final double price;MedicineChoice(int i,String n,int s,double p){id=i;name=n;stock=s;price=p;}
        @Override public String toString(){return name+" (stock "+stock+")";}
    }
    public static class SaleRow{
        private final Integer id,quantity;private final String medicine,date;private final Double unitPrice,total;
        SaleRow(int i,String m,int q,double p,double t,String d){id=i;medicine=m;quantity=q;unitPrice=p;total=t;date=d;}
        public Integer getId(){return id;}public String getMedicine(){return medicine;}public Integer getQuantity(){return quantity;}public Double getUnitPrice(){return unitPrice;}public Double getTotal(){return total;}public String getDate(){return date;}
    }

    private static Node securityView(String username) {
        PasswordField current=new PasswordField();current.setPromptText("Current password");
        PasswordField next=new PasswordField();next.setPromptText("New password (at least 10 characters)");
        PasswordField confirm=new PasswordField();confirm.setPromptText("Confirm new password");
        Button change=button("Change password");change.getStyleClass().add("primary");
        change.setOnAction(e->{if(!AuthService.authenticate(username,current.getText())){error("Password not changed","Current password is incorrect.");return;}if(!next.getText().equals(confirm.getText())){error("Password not changed","New passwords do not match.");return;}try{AuthService.changePassword(username,next.getText());current.clear();next.clear();confirm.clear();info("Updated","Password changed successfully.");}catch(Exception ex){error("Password not changed",ex.getMessage());}});
        VBox form=card(label("Update the password for the current account."),current,next,confirm,change);
        Label note=label("Use a unique password. This starter does not yet include multi-user role management, password recovery, or an audit log.");note.setWrapText(true);
        return page("Account Security","Change the password for the signed-in account.",form,card(note));
    }

    private static <T> TableColumn<T, ?> col(String title,String property,double width) {
        TableColumn<T,Object> c=new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        return c;
    }
}
