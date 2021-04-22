package edu.swufe.nxksecdisk;

import edu.swufe.nxksecdisk.launcher.ConsoleLauncher;
import edu.swufe.nxksecdisk.launcher.UiLauncher;

/**
 * <h2>kiftd主类（启动类）</h2>
 * <p>
 * 该类为程序主类，内部的main方法为kiftd程序的唯一入口。您可以从这里开始逐步了解kiftd的整个设计。
 * 如果需反馈问题，欢迎随时与作者联系：kohgylw@163.com，您的反馈将是kiftd不断完善的最佳动力。
 * </p>
 * <h4>许可声明：</h4>
 * <p>
 * 使用该源代码（包括但不限于：分发、修改、编译）代表您接受以下条款：
 * </p>
 * <ul>
 * <li>任何人均可以免费获取kiftd的源代码的原版拷贝，并进行分发或修改，并可用于任何用途。</li>
 * <li>经由该源代码或其修改版本编译而成的程序也可以运用于任何用途，无论是商业的还是非商业的。</li>
 * <li>作者青阳龙野（kohgylw@163.com）无需为使用该源代码或其编译生成的程序所导致的任何后果承担责任。</li>
 * <li>作者青阳龙野（kohgylw@163.com）保留kiftd原版源代码及其编译生成的程序的版权。</li>
 * </ul>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */

public class Application {

    /**
     * <h2>主方法</h2>
     * <p>
     * 这里是整个kiftd应用的入口，即程序的主方法。您可以从此开始阅读kiftd的全部逻辑代码。
     * </p>
     *
     * @param args java.lang.String[] 接收控制台传入参数，例如“-console“
     * @author 青阳龙野(kohgylw)
     */
	
    public static void main(final String[] args) {
        if (args == null || args.length == 0) {
            // 以界面模式启动kiftd;
            UiLauncher.launch();
        } else {
            // 以控制台模式启动kiftd;
            ConsoleLauncher.launch(args);
        }
    }
}
