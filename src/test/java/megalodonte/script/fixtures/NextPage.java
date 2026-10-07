package megalodonte.script.fixtures;

import megalodonte.script.ViewScript;

public final class NextPage extends ViewScript {
    public static int mounts;
    public static int destroys;
    @Override public void onSubmit() {}
    @Override public void onMount() { mounts++; }
    @Override public void onDestroy() { destroys++; }
}
