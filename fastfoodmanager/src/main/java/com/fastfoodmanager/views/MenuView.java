package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Menu;
import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.Product.ProductCategory;
import com.fastfoodmanager.domain.MenuCardSettings;
import com.fastfoodmanager.service.MenusService;
import com.fastfoodmanager.service.MenuCardSettingsService;
import com.fastfoodmanager.service.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.io.IOException;
import java.util.List;

@Route(value = "menu", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class MenuView extends VerticalLayout {

    private final ProductService productService;
    private final MenusService menusService;

    // ===== FORM FIELDS =====
    private TextField nameField = new TextField("Nombre del menú");
    private TextArea descriptionField = new TextArea("Descripción");
    private NumberField priceField = new NumberField("Precio (€)");
    private Checkbox activeField = new Checkbox("Activo");

    private byte[] imageBytes;
    private Image preview = new Image();

    // Selectores + cantidades
    private MultiSelectComboBox<Product> mainSelector = new MultiSelectComboBox<>("Main");
    private NumberField mainQty = new NumberField("Cantidad Main");

    private MultiSelectComboBox<Product> sideSelector = new MultiSelectComboBox<>("Side");
    private NumberField sideQty = new NumberField("Cantidad Side");

    private MultiSelectComboBox<Product> drinkSelector = new MultiSelectComboBox<>("Drink");
    private NumberField drinkQty = new NumberField("Cantidad Drink");

    private MultiSelectComboBox<Product> secondarySelector = new MultiSelectComboBox<>("Secondary");
    private NumberField secondaryQty = new NumberField("Cantidad Secondary");

    private MultiSelectComboBox<Product> dessertSelector = new MultiSelectComboBox<>("Dessert");
    private NumberField dessertQty = new NumberField("Cantidad Dessert");

    // ===== GRID =====
    private Grid<Menu> grid = new Grid<>(Menu.class, false);

    private Menu current = new Menu();

    private final MenuCardSettingsService cardSettingsService;

    public MenuView(ProductService productService, MenusService menusService, MenuCardSettingsService cardSettingsService) {
        this.productService = productService;
        this.menusService = menusService;
        this.cardSettingsService = cardSettingsService;

        setSpacing(true);
        setPadding(true);

        configureSelectors();
        configureGrid();

        add(buildForm(), grid);

        loadMenus();
    }

    // ============================================================
    // CONFIGURAR SELECTORES
    // ============================================================
    private void configureSelectors() {
        List<Product> products = productService.findAll();

        mainSelector.setItems(products.stream().filter(p -> p.getCategory() == ProductCategory.MAIN).toList());
        sideSelector.setItems(products.stream().filter(p -> p.getCategory() == ProductCategory.SIDE).toList());
        drinkSelector.setItems(products.stream().filter(p -> p.getCategory() == ProductCategory.DRINK).toList());
        secondarySelector.setItems(products.stream().filter(p -> p.getCategory() == ProductCategory.SECONDARY).toList());
        dessertSelector.setItems(products.stream().filter(p -> p.getCategory() == ProductCategory.DESSERT).toList());

        mainSelector.setItemLabelGenerator(Product::getName);
        sideSelector.setItemLabelGenerator(Product::getName);
        drinkSelector.setItemLabelGenerator(Product::getName);
        secondarySelector.setItemLabelGenerator(Product::getName);
        dessertSelector.setItemLabelGenerator(Product::getName);

        mainQty.setMin(1);
        sideQty.setMin(1);
        drinkQty.setMin(1);

        secondaryQty.setMin(0);
        dessertQty.setMin(0);
    }

    // ============================================================
    // FORMULARIO
    // ============================================================
    private VerticalLayout buildForm() {

        FormLayout form = new FormLayout();

        descriptionField.setWidthFull();
        descriptionField.setMaxLength(800);

        // Upload imagen
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/*");
        upload.addSucceededListener(e -> {
            try {
                imageBytes = buffer.getInputStream().readAllBytes();
                preview.setSrc("data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes));
                preview.setWidth("150px");
            } catch (IOException ex) {
                Notification.show("Error al cargar imagen");
            }
        });

        // Añadir campos al formulario
        form.add(
                nameField, priceField, activeField,
                descriptionField,
                mainSelector, mainQty,
                sideSelector, sideQty,
                drinkSelector, drinkQty,
                secondarySelector, secondaryQty,
                dessertSelector, dessertQty,
                upload, preview
        );

        form.setColspan(descriptionField, 2);
        form.setColspan(upload, 2);
        form.setColspan(preview, 2);

        Button save = new Button("Guardar", e -> saveMenu());
        Button clear = new Button("Limpiar", e -> resetForm());
        Button delete = new Button("Eliminar", e -> deleteMenu());
        Button editCardboardBtn = new Button("Modificar Cardboard Menús");
        editCardboardBtn.addClickListener(e -> openCardboardEditor());
        add(editCardboardBtn);


        HorizontalLayout actions = new HorizontalLayout(save, clear, delete);

        return new VerticalLayout(form, actions);
    }

    // ============================================================
    // GRID
    // ============================================================
    private void configureGrid() {
        grid.addColumn(Menu::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Menu::getName).setHeader("Nombre");
        grid.addColumn(Menu::getPrice).setHeader("Precio");
        grid.addColumn(Menu::isActive).setHeader("Activo");

        grid.asSingleSelect().addValueChangeListener(e -> {
            Menu selected = e.getValue();
            if (selected != null) loadMenu(selected);
        });
    }

    private void loadMenus() {
        grid.setItems(menusService.findAll());
    }

    // ============================================================
    // CRUD
    // ============================================================
    private void loadMenu(Menu menu) {
        current = menu;

        nameField.setValue(menu.getName());
        priceField.setValue(menu.getPrice());
        activeField.setValue(menu.isActive());
        descriptionField.setValue(menu.getDescription() != null ? menu.getDescription() : "");

        imageBytes = menu.getImage();
        if (imageBytes != null)
            preview.setSrc("data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes));

        mainSelector.setValue(menu.getMainProducts());
        sideSelector.setValue(menu.getSideProducts());
        drinkSelector.setValue(menu.getDrinkProducts());
        secondarySelector.setValue(menu.getSecondaryProducts());
        dessertSelector.setValue(menu.getDessertProducts());

        // Cargar cantidades
        mainQty.setValue((double) menu.getMainQuantity());
        sideQty.setValue((double) menu.getSideQuantity());
        drinkQty.setValue((double) menu.getDrinkQuantity());
        secondaryQty.setValue((double) menu.getSecondaryQuantity());
        dessertQty.setValue((double) menu.getDessertQuantity());
    }

    private void saveMenu() {

        // VALIDACIONES OBLIGATORIAS
        if (mainSelector.getSelectedItems().isEmpty()) {
            Notification.show("Debes seleccionar al menos un producto Main");
            return;
        }
        if (sideSelector.getSelectedItems().isEmpty()) {
            Notification.show("Debes seleccionar al menos un producto Side");
            return;
        }
        if (drinkSelector.getSelectedItems().isEmpty()) {
            Notification.show("Debes seleccionar al menos un producto Drink");
            return;
        }

        current.setName(nameField.getValue());
        current.setDescription(descriptionField.getValue());
        current.setPrice(priceField.getValue());
        current.setActive(activeField.getValue());
        current.setImage(imageBytes);

        current.setMainProducts(mainSelector.getSelectedItems().stream().toList());
        current.setSideProducts(sideSelector.getSelectedItems().stream().toList());
        current.setDrinkProducts(drinkSelector.getSelectedItems().stream().toList());
        current.setSecondaryProducts(secondarySelector.getSelectedItems().stream().toList());
        current.setDessertProducts(dessertSelector.getSelectedItems().stream().toList());

        // Guardar cantidades
        current.setMainQuantity(mainQty.getValue().intValue());
        current.setSideQuantity(sideQty.getValue().intValue());
        current.setDrinkQuantity(drinkQty.getValue().intValue());
        current.setSecondaryQuantity(secondaryQty.getValue().intValue());
        current.setDessertQuantity(dessertQty.getValue().intValue());

        menusService.save(current);

        Notification.show("Menú guardado correctamente");
        loadMenus();
        resetForm();
    }

    private void deleteMenu() {
        if (current.getId() == null) {
            Notification.show("Selecciona un menú para eliminar");
            return;
        }

        menusService.delete(current.getId());
        Notification.show("Menú eliminado");

        loadMenus();
        resetForm();
    }

    private void resetForm() {
        current = new Menu();
        nameField.clear();
        priceField.clear();
        descriptionField.clear();
        activeField.setValue(false);

        mainSelector.clear();
        sideSelector.clear();
        drinkSelector.clear();
        secondarySelector.clear();
        dessertSelector.clear();

        mainQty.clear();
        sideQty.clear();
        drinkQty.clear();
        secondaryQty.clear();
        dessertQty.clear();

        preview.setSrc("");
        imageBytes = null;

        grid.deselectAll();
    }

    private void openCardboardEditor() {
        MenuCardSettings settings = cardSettingsService.get();

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Modificar Cardboard Menús");

        TextField nameField = new TextField("Nombre del Cardboard");
        nameField.setValue(settings.getName());

        Image preview = new Image();
        preview.setWidth("200px");

        if (settings.getImage() != null) {
            preview.setSrc("data:image/png;base64," +
                    java.util.Base64.getEncoder().encodeToString(settings.getImage()));
        }

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/*");

        upload.addSucceededListener(e -> {
            try {
                byte[] bytes = buffer.getInputStream().readAllBytes();
                preview.setSrc("data:image/png;base64," +
                        java.util.Base64.getEncoder().encodeToString(bytes));
                settings.setImage(bytes);
            } catch (Exception ex) {
                Notification.show("Error al cargar imagen");
            }
        });

        Button save = new Button("Guardar", ev -> {
            settings.setName(nameField.getValue());
            cardSettingsService.save(settings);
            Notification.show("Cardboard actualizado");
            dialog.close();
        });

        dialog.add(nameField, upload, preview);
        dialog.getFooter().add(save, new Button("Cerrar", ev -> dialog.close()));
        dialog.open();
    }
}
