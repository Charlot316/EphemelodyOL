import { createRouter, createWebHistory } from "vue-router";

const routes = [{
        path: "/",
        //redirect: "/chart/maker",
        redirect: "/home",
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
        path: "/home",
        name: "Home",
        component: () =>
            import (
                "../views/Home.vue"
            ),
    },
    {
        path: "/register",
        name: "register",
        component: () =>
            import (
                "../views/Register.vue"
            ),
    },
    {
        path: "/login",
        name: "login",
        component: () =>
            import (
                "../views/Login.vue"
            ),
    },
    {
        path: "/select",
        name: "Select",
        component: () =>
            import (
                "../views/Select.vue"
            ),
    },
    {
        path: "/admin",
        name: "Admin",
        component: () =>
            import (
                "../views/Admin.vue"
            ),
    },
];

const router = createRouter({
    history: createWebHistory("/"),
    routes,
});

export default router;