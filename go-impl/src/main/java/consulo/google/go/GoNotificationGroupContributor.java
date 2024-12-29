package consulo.google.go;

import com.goide.GoConstants;
import consulo.annotation.component.ExtensionImpl;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.notification.NotificationGroupContributor;

import jakarta.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 08-Aug-22
 */
@ExtensionImpl
public class GoNotificationGroupContributor implements NotificationGroupContributor {
  @Override
  public void contribute(@Nonnull Consumer<NotificationGroup> consumer) {
    consumer.accept(GoConstants.GO_NOTIFICATION_GROUP);
    consumer.accept(GoConstants.GO_EXECUTION_NOTIFICATION_GROUP);
  }
}
