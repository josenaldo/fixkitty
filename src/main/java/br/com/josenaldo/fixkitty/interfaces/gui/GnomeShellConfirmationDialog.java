package br.com.josenaldo.fixkitty.interfaces.gui;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.util.Optional;

/**
 * Confirmation dialog shown before executing Fix GNOME Shell.
 *
 * <p>Warns the user that the graphical interface will briefly close and
 * FixKitty will relaunch automatically. Requires explicit confirmation.
 */
public class GnomeShellConfirmationDialog {

    /**
     * Shows the confirmation dialog and returns {@code true} if the user confirms.
     *
     * @return {@code true} if the user clicked "Confirm"; {@code false} if cancelled
     */
    public boolean confirm() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Fix GNOME Shell");
        alert.setHeaderText("This action will restart the GNOME shell.");
        alert.setContentText(
            "The graphical interface will briefly close and FixKitty " +
            "will relaunch automatically. Continue?");

        ButtonType confirmBtn = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmBtn, cancelBtn);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmBtn;
    }
}
