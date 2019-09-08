package ifmo.programming.lab7.transmitter;

/**
 * Используется, чтобы не пероеопределять все методы {@link SenderListener}
 */
public class SenderAdapter implements SenderListener {
    @Override
    public void onSuccess() {}

    @Override
    public void onProgress(float progress) {}

    @Override
    public void onError(String message) {}
}
