package mod.codewarrior.sips.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.widget.EntryWidget;
import net.minecraft.client.util.math.MatrixStack;

public class SipsWidget extends EntryWidget {

    protected SipsWidget(int x, int y) {
        super(x, y);
        this.getBounds().setSize(80, 88);
    }

    protected Rectangle getSipBounds() {
        return new Rectangle(this.getBounds().x - 6, this.getBounds().y, 88, 88);
    }

    @Override
    protected void drawCurrentEntry(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        EntryStack entry = this.getCurrentEntry();
        entry.setZ(100);
        entry.render(matrices, this.getSipBounds(), mouseX, mouseY, delta);
    }
}
