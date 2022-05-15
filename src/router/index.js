import { createRouter, createWebHistory } from "vue-router";
import Home from "../views/Home.vue";

const routes = [{
        path: "/",
        // redirect: "/chart/maker",
        redirect: "/play",
    },
    {
        path: "/play",
        name: "Play",
        component: () =>
            import (
                "../views/PlayInterface.vue"
            ),
    },
    {
        path: "/chart/maker",
        name: "chartMaker",
        component: () =>
            import (
                "../views/ChartMaker.vue"
            ),
    },
    // {
    //   path: "/register",
    //   name: "Register",
    //   component: () =>
    //     import(
    //       /* webpackChunkName: "login" */
    //       "../views/Register.vue"
    //     ),
    // },
    {
        path: "/",
        name: "Home", //Home组件里包括了初始的导航栏和侧边栏的样式，它的所有children都会共享Home里的导航栏、侧边栏
        component: Home,
        children: [
            // {
            //     path: "user",
            //     name: "user",
            //     meta: {
            //         title: '个人中心',
            //         requireAuth: true,
            //         keepAlive: true
            //     },
            //     component: () => import("../views/User.vue")
            // },
        ],
    },
];

const router = createRouter({
    history: createWebHistory("/"),
    routes,
});

export default router;