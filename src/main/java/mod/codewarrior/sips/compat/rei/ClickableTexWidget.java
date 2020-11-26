package mod.codewarrior.sips.compat.rei;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.gui.widget.EntryWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClickableTexWidget extends EntryWidget {
    protected ClickableTexWidget(Rectangle rect) {
        super(rect.getMinX(), rect.getMinY());
        this.getBounds().setSize(rect.getWidth(), rect.getHeight());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (interactable && containsMouse(mouseX, mouseY))
            return ClientHelper.getInstance().openView(ClientHelper.ViewSearchBuilder.builder().addCategory(SipsREIPlugin.BEVERAGES).fillPreferredOpenedCategory());
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void queueTooltip(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        Text text = new TranslatableText("rei.sips.show_category");
        Tooltip tooltip = Tooltip.create(new Point(mouseX, mouseY), text);
        tooltip.queue();
    }
}
