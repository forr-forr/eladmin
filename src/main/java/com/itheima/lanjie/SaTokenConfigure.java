package com.itheima.lanjie;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            String requestPath = SaHolder.getRequest().getRequestPath();
            System.out.println("拦截请求路径：" + requestPath);
            try {
                StpUtil.checkLogin(); // 校验登录状态
            } catch (NotLoginException e) {
                SaHolder.getResponse().redirect("/login.html");
            }
        }))
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns(  // 放行静态资源
                        "/**/*.html",        // 放行所有html文件
                        "/**/*.js",          // 放行所有js文件
                        "/**/*.css",         // 放行所有css文件
                        "/static/**",        // 放行静态资源目录下的文件
                        "/images/**",        // 放行图片
                        "/js/**",            // 放行js文件
                        "/css/**",           // 放行css文件
                        "/element-ui/**"     // 放行 Element UI 相关静态文件
                )
                .excludePathPatterns(  // 放行一些常见的路径
                        "/favicon.ico",
                        "/error",            // 错误页面
                        "/register.html",    // 放行注册页面
                        "/login.html",       // 放行登录页面
                        "/index.html",       // 放行首页
                        "/user/doLogin",     // 放行登录接口
                        "/",     // 放行登录接口
                        "/user/doRegister"    // 放行注册接口
                );
    }

    /**
     * 注册 [Sa-Token全局过滤器]
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()

                // 指定 拦截路由 与 放行路由
                .addInclude("/**")
                .addExclude(
                        "/favicon.ico",
                        "/**/*.html", "/**/*.js", "/**/*.css",
                        "/static/**", "/images/**", "/js/**", "/css/**", "/element-ui/**",
                        "/user/doLogin", "/user/doRegister", "/login.html", "/register.html", "/index.html", "/"
                )
                // 认证函数: 每次请求执行
                .setAuth(obj -> {

                    // 登录认证 -- 拦截所有路由，并排除/user/doLogin 用于开放登录
                    SaRouter.match("/**", "/user/doLogin", () -> StpUtil.checkLogin());

                    // 更多拦截处理方式，请参考“路由拦截式鉴权”章节 */
                })

                // 异常处理函数：每次认证函数发生异常时执行此函数
                .setError(e -> {
                    return SaResult.error(e.getMessage());
                })

                // 前置函数：在每次认证函数之前执行（BeforeAuth 不受 includeList 与 excludeList 的限制，所有请求都会进入）
                .setBeforeAuth(r -> {
                    // ---------- 设置一些安全响应头 ----------
                    SaHolder.getResponse()
                            // 服务器名称
                            .setServer("sa-server")
                            // 是否可以在iframe显示视图： DENY=不可以 | SAMEORIGIN=同域下可以 | ALLOW-FROM uri=指定域名下可以
                            .setHeader("X-Frame-Options", "SAMEORIGIN")
                            // 是否启用浏览器默认XSS防护： 0=禁用 | 1=启用 | 1; mode=block 启用, 并在检查到XSS攻击时，停止渲染页面
                            .setHeader("X-XSS-Protection", "1; mode=block")
                            // 禁用浏览器内容嗅探
                            .setHeader("X-Content-Type-Options", "nosniff");
                    ;
                })
                ;
    }
}
