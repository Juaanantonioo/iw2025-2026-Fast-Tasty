package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.*;
import com.fastfoodmanager.service.OfferService;
import com.fastfoodmanager.service.ProductService;
import com.fastfoodmanager.service.FoodTypeService;
import com.fastfoodmanager.service.OfferCardSettingsService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "admin/ofertas", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminOffersView extends VerticalLayout {

    private final OfferService offerService;
    private final ProductService productService;
    private final FoodTypeService foodTypeService;
    private final OfferCardSettingsService cardSettingsService;

    private Grid<Offer> grid = new Grid<>(Offer.class, false);

    private TextField nameField = new TextField("Nombre de la oferta");

    private ComboBox<OfferTarget> targetSelect = new ComboBox<>("Tipo de objetivo");
    private ComboBox<OfferMode> modeSelect = new ComboBox<>("Tipo de oferta");

    private IntegerField discountField = new IntegerField("Descuento (%)");
    private IntegerField zField = new IntegerField("Z (cantidad total)");
    private IntegerField yField = new IntegerField("Y (cantidad a pagar)");

    private MultiSelectComboBox<Product> productSelector = new MultiSelectComboBox<>("Productos afectados");
    private MultiSelectComboBox<FoodType> categorySelector = new MultiSelectComboBox<>("Categorías afectadas");

    private Button saveBtn = new Button("Guardar");
    private Button deleteBtn = new Button("Eliminar");
    private Button newBtn = new Button("➕ Nueva oferta");
    private Button editCardboard = new Button("Editar Cardboard Ofertas");

    private Offer selected;

    // 🔥 NUEVOS CAMPOS PARA IMAGEN DE LA OFERTA
    private Upload offerImageUpload;
    private Image offerPreview;
    private byte[] offerImageBytes;

    public AdminOffersView(OfferService offerService,
                           ProductService productService,
                           FoodTypeService foodTypeService,
                           OfferCardSettingsService cardSettingsService) {

        this.offerService = offerService;
        this.productService = productService;
        this.foodTypeService = foodTypeService;
        this.cardSettingsService = cardSettingsService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        configureSelectors();
        configureButtons();

        Component form = createForm(); // ← crear UNA sola vez

        HorizontalLayout content = new HorizontalLayout(
                grid,
                form
        );
        content.setHeight("calc(100vh - 120px)");
        form.getElement().getStyle().set("height", "100%");

        content.setSizeFull();
        content.setFlexGrow(1, grid);
        content.setFlexGrow(0, form); // ← usar el MISMO form

        add(new H2("Gestión de Ofertas"), content);
        editCardboard.addClickListener(e -> openCardboardEditor());

        refresh();
    }

    // ============================================================
    // GRID
    // ============================================================

    private void configureGrid() {
        grid.addColumn(Offer::getName).setHeader("Nombre");
        grid.addColumn(o -> o.getTargetType().name()).setHeader("Objetivo");
        grid.addColumn(o -> o.getMode().name()).setHeader("Modo");
        grid.addColumn(o -> o.isActive() ? "Sí" : "No").setHeader("Activa");

        grid.setSizeFull();

        grid.asSingleSelect().addValueChangeListener(e -> {
            selected = e.getValue();
            loadOfferIntoForm();
        });
    }

    // ============================================================
    // FORM
    // ============================================================

    private Component createForm() {

        discountField.setMin(1);
        discountField.setMax(100);

        zField.setMin(1);
        yField.setMin(1);

        productSelector.setItems(productService.findAll());
        productSelector.setItemLabelGenerator(Product::getName);

        categorySelector.setItems(foodTypeService.findAll());
        categorySelector.setItemLabelGenerator(FoodType::getName);

        offerPreview = new Image();
        offerPreview.setWidth("180px");
        offerPreview.getStyle().set("border-radius", "12px");

        MemoryBuffer offerBuffer = new MemoryBuffer();
        offerImageUpload = new Upload(offerBuffer);
        offerImageUpload.setAcceptedFileTypes("image/png", "image/jpeg");

        offerImageUpload.addSucceededListener(ev -> {
            try {
                offerImageBytes = offerBuffer.getInputStream().readAllBytes();
                offerPreview.setSrc("data:image/png;base64," +
                        java.util.Base64.getEncoder().encodeToString(offerImageBytes));
                Notification.show("Imagen cargada correctamente");
            } catch (Exception ex) {
                Notification.show("Error al leer la imagen");
            }
        });

        VerticalLayout form = new VerticalLayout(
                newBtn,
                editCardboard,
                nameField,
                targetSelect,
                modeSelect,
                discountField,
                zField,
                yField,
                productSelector,
                categorySelector,
                new H3("Imagen de la oferta"),
                offerPreview,
                offerImageUpload,
                saveBtn,
                deleteBtn
        );

        // ======== ESTILOS ORIGINALES ========
        form.setWidth("380px");
        form.setPadding(true);
        form.setSpacing(true);
        form.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-base-color)");

        // ❌ IMPORTANTE: NO poner overflow en el form
        // form.getStyle().set("overflow-y", "auto");  ← ELIMINADO

        // ======== CONTENEDOR CON SCROLL (NO PADRE DIRECTO DE COMBOBOX) ========
        Div scrollContainer = new Div();
        scrollContainer.setHeight("100%");
        scrollContainer.getStyle().set("overflow-y", "auto");

        scrollContainer.add(form);

        return scrollContainer;
    }

    private void configureSelectors() {

        targetSelect.setItems(OfferTarget.values());
        modeSelect.setItems(OfferMode.values());

        targetSelect.addValueChangeListener(e -> updateVisibility());
        modeSelect.addValueChangeListener(e -> updateVisibility());

        updateVisibility();
    }

    private void updateVisibility() {

        discountField.setVisible(false);
        zField.setVisible(false);
        yField.setVisible(false);
        productSelector.setVisible(false);
        categorySelector.setVisible(false);

        if (modeSelect.getValue() == OfferMode.DISCOUNT) {
            discountField.setVisible(true);
        }

        if (modeSelect.getValue() == OfferMode.ZxY) {
            zField.setVisible(true);
            yField.setVisible(true);
        }

        if (targetSelect.getValue() == OfferTarget.PRODUCT) {
            productSelector.setVisible(true);
        }

        if (targetSelect.getValue() == OfferTarget.CATEGORY) {
            categorySelector.setVisible(true);
        }
    }

    // ============================================================
    // BUTTONS
    // ============================================================

    private void configureButtons() {

        newBtn.addClickListener(e -> newOffer());

        saveBtn.addClickListener(e -> save());

        deleteBtn.addClickListener(e -> confirmDelete());
        deleteBtn.setEnabled(false);
    }

    private void newOffer() {
        grid.deselectAll();
        selected = null;

        nameField.clear();
        targetSelect.clear();
        modeSelect.clear();
        discountField.clear();
        zField.clear();
        yField.clear();
        productSelector.clear();
        categorySelector.clear();

        offerPreview.setSrc("");
        offerImageBytes = null;

        deleteBtn.setEnabled(false);
    }

    private void loadOfferIntoForm() {
        if (selected == null) return;

        nameField.setValue(selected.getName());
        targetSelect.setValue(selected.getTargetType());
        modeSelect.setValue(selected.getMode());

        if (selected.getDiscountPercentage() != null)
            discountField.setValue(selected.getDiscountPercentage());

        if (selected.getZValue() != null)
            zField.setValue(selected.getZValue());

        if (selected.getYValue() != null)
            yField.setValue(selected.getYValue());

        if (selected.getProducts() != null)
            productSelector.setValue(selected.getProducts());

        if (selected.getCategories() != null)
            categorySelector.setValue(selected.getCategories());

        // 🔥 CARGAR IMAGEN
        if (selected.getImage() != null) {
            offerPreview.setSrc("data:image/png;base64," +
                    java.util.Base64.getEncoder().encodeToString(selected.getImage()));
            offerImageBytes = selected.getImage();
        } else {
            offerPreview.setSrc("");
            offerImageBytes = null;
        }

        deleteBtn.setEnabled(true);

        updateVisibility();
    }

    private void save() {

        if (nameField.isEmpty() || targetSelect.isEmpty() || modeSelect.isEmpty()) {
            Notification.show("Rellena todos los campos obligatorios");
            return;
        }

        boolean isNew = (selected == null);

        if (selected == null)
            selected = new Offer();

        selected.setName(nameField.getValue());
        selected.setTargetType(targetSelect.getValue());
        selected.setMode(modeSelect.getValue());

        if (modeSelect.getValue() == OfferMode.DISCOUNT) {
            selected.setDiscountPercentage(discountField.getValue());
        } else {
            selected.setZValue(zField.getValue());
            selected.setYValue(yField.getValue());
        }

        // ===============================
        // GUARDAR PRODUCTOS SEGÚN TARGET
        // ===============================

        switch (targetSelect.getValue()) {

            case PRODUCT -> {
                selected.setProducts(productSelector.getValue().stream().toList());
                selected.setCategories(null);
            }

            case CATEGORY -> {
                List<FoodType> cats = categorySelector.getValue().stream().toList();
                selected.setCategories(cats);

                List<Product> prods = productService.findAll().stream()
                        .filter(p -> p.getType() != null && cats.contains(p.getType()))
                        .toList();

                selected.setProducts(prods);
            }

            case GLOBAL -> {
                selected.setCategories(null);
                selected.setProducts(productService.findAll());
            }
        }

        // 🔥 GUARDAR IMAGEN
        if (offerImageBytes != null) {
            selected.setImage(offerImageBytes);
        }

        Offer saved = offerService.save(selected);

        List<String> conflicts = offerService.findConflicts(saved);

        if (!conflicts.isEmpty()) {

            if (isNew) {
                offerService.delete(saved);
            }

            Notification.show(
                    "No se puede crear la oferta:\n" + String.join("\n", conflicts),
                    5000,
                    Notification.Position.MIDDLE
            );
            return;
        }

        offerService.removeLowerPriorityAssignments(saved);

        refresh();
        newOffer();
        Notification.show("Oferta guardada");
    }

    private void confirmDelete() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Eliminar oferta");
        dialog.setText("¿Seguro que deseas eliminar esta oferta?");

        dialog.setCancelable(true);
        dialog.setConfirmText("Eliminar");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            offerService.delete(selected);
            newOffer();
            refresh();
            Notification.show("Oferta eliminada");
        });

        dialog.open();
    }

    private void openCardboardEditor() {
        OfferCardSettings settings = cardSettingsService.get();

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Editar Cardboard de Ofertas");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.addSucceededListener(ev -> {
            try {
                byte[] uploadedBytes = buffer.getInputStream().readAllBytes();
                settings.setImage(uploadedBytes);
                Notification.show("Imagen cargada correctamente", 2000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Error al leer la imagen", 3000, Notification.Position.MIDDLE);
            }
        });

        TextField name = new TextField("Nombre");
        name.setValue(settings.getName());

        Button save = new Button("Guardar", e -> {
            settings.setName(name.getValue());
            cardSettingsService.save(settings);
            dialog.close();
        });

        dialog.add(name, upload);
        dialog.getFooter().add(save);
        dialog.open();
    }

    private void refresh() {
        grid.setItems(offerService.findAll());
    }
}
