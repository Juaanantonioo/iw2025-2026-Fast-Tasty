package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.FoodType;
import com.fastfoodmanager.service.FoodTypeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

@Route(value = "admin/categorias", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminFoodTypeView extends VerticalLayout {

    private final FoodTypeService foodTypeService;

    private Grid<FoodType> grid = new Grid<>(FoodType.class, false);
    private TextField nameField = new TextField("Nombre de la categoría");

    // Imagen
    private MemoryBuffer imageBuffer = new MemoryBuffer();
    private Upload imageUpload = new Upload(imageBuffer);
    private Image previewImage = new Image();

    private Button saveBtn = new Button("Guardar");
    private Button deleteBtn = new Button("Eliminar");
    private Button newBtn = new Button("➕ Nueva categoría");

    private FoodType selected;

    public AdminFoodTypeView(FoodTypeService foodTypeService) {
        this.foodTypeService = foodTypeService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        configureButtons();

        HorizontalLayout content = new HorizontalLayout(
                grid,
                createForm()
        );

        content.setSizeFull();
        content.setFlexGrow(1, grid);

        add(
                new H2("Gestión de Categorías"),
                content
        );

        refresh();
    }

    // ---------- GRID ----------
    private void configureGrid() {
        grid.addColumn(FoodType::getName).setHeader("Categoría");
        grid.setSizeFull();

        grid.asSingleSelect().addValueChangeListener(e -> {
            selected = e.getValue();

            if (selected != null) {
                nameField.setValue(selected.getName());
                deleteBtn.setEnabled(true);

                // Mostrar imagen
                if (selected.getImage() != null) {
                    String base64 = Base64.getEncoder().encodeToString(selected.getImage());
                    previewImage.setSrc("data:image/png;base64," + base64);
                } else {
                    previewImage.setSrc("");
                }
            }
        });
    }

    // ---------- FORM ----------
    private VerticalLayout createForm() {
        previewImage.setWidth("200px");
        previewImage.setHeight("150px");

        imageUpload.setAcceptedFileTypes("image/jpeg", "image/png");
        imageUpload.setMaxFiles(1);
        imageUpload.setDropLabel(new Span("Arrastra o selecciona la imagen (400x300 px)"));
        imageUpload.setWidthFull();

        imageUpload.addSucceededListener(event -> {
            try (InputStream is = imageBuffer.getInputStream()) {
                BufferedImage bufferedImage = ImageIO.read(is);
                if (bufferedImage == null) {
                    Notification.show("Formato de imagen no válido", 3000, Notification.Position.TOP_CENTER);
                    imageUpload.clearFileList();
                    previewImage.setSrc("");
                    return;
                }

                if (bufferedImage.getWidth() != 400 || bufferedImage.getHeight() != 300) {
                    Notification.show("La imagen debe tener 400x300 px", 3000, Notification.Position.TOP_CENTER);
                    imageUpload.clearFileList();
                    previewImage.setSrc("");
                    return;
                }

                // Preview
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                previewImage.setSrc("data:image/png;base64," + base64);

            } catch (Exception ex) {
                Notification.show("Error al procesar la imagen", 3000, Notification.Position.TOP_CENTER);
                imageUpload.clearFileList();
                previewImage.setSrc("");
            }
        });

        VerticalLayout form = new VerticalLayout(
                newBtn,
                nameField,
                imageUpload,
                previewImage,
                saveBtn,
                deleteBtn
        );

        form.setWidth("320px");
        form.setPadding(true);
        form.setSpacing(true);
        form.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-base-color)");

        return form;
    }

    private void configureButtons() {
        newBtn.addClickListener(e -> newCategory());

        saveBtn.addClickListener(e -> save());

        deleteBtn.addClickListener(e -> confirmDelete());
        deleteBtn.setEnabled(false);
    }

    // ---------- ACTIONS ----------
    private void newCategory() {
        grid.deselectAll();
        selected = null;
        nameField.clear();
        previewImage.setSrc("");
        imageUpload.clearFileList();
        deleteBtn.setEnabled(false);
        nameField.focus();
    }

    private void save() {
        if (nameField.isEmpty()) {
            Notification.show("El nombre es obligatorio");
            return;
        }

        if (selected == null) {
            selected = new FoodType();
        }

        selected.setName(nameField.getValue());

        // Guardar imagen
        try {
            if (imageBuffer.getInputStream() != null) {
                try (InputStream is = imageBuffer.getInputStream();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    BufferedImage bufferedImage = ImageIO.read(is);
                    ImageIO.write(bufferedImage, "png", baos);
                    selected.setImage(baos.toByteArray());
                }
            }
        } catch (Exception ex) {
            Notification.show("Error al guardar la imagen", 3000, Notification.Position.TOP_CENTER);
            return;
        }

        foodTypeService.save(selected);

        refresh();
        newCategory();
        Notification.show("Categoría guardada");
    }

    private void confirmDelete() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Eliminar categoría");
        dialog.setText(
                "⚠️ Se eliminarán todos los productos de esta categoría.\n\n¿Deseas continuar?"
        );

        dialog.setCancelable(true);
        dialog.setConfirmText("Eliminar");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            foodTypeService.deleteFoodType(selected);
            newCategory();
            refresh();
            Notification.show("Categoría eliminada");
        });

        dialog.open();
    }

    // ---------- UTILS ----------
    private void refresh() {
        grid.setItems(foodTypeService.findAll());
    }
}
