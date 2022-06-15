import { createRouter, createWebHistory } from "vue-router";

const routes = [{
        path: "/",
        //redirect: "/chart/maker",
        redirect: "/home",
    },
    {
        path: "/play",
        name: "Play",
        meta: {
            requireAuth: true,
        },
        component: () =>
            import (
                "../views/PlayInterface.vue"
            ),
    },
    {
        path: "/chart/maker",
        name: "chartMaker",
        meta: {
            requireAuth: true,
        },
        component: () =>
            import (
                "../views/ChartMaker.vue"
            ),
    },
    {
        path: "/home",
        name: "Home",
        meta: {
            requireAuth: true,
        },
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
        path: "/public",
        name: "Public",
        meta: {
            requireAuth: true,
        },
        component: () =>
            import (
                "../views/Select.vue"
            ),
    },
    {
        path: "/society",
        name: "Society",
        meta: {
            requireAuth: true,
        },
        component: () =>
            import (
                "../views/Select.vue"
            ),
    },
    {
        path: "/admin",
        name: "Admin",
        meta: {
            requireAuth: true,
        },
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