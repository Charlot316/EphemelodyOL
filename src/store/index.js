import { createStore } from 'vuex'
import createPersistedState from "vuex-persistedstate"
export default createStore({
    state: {
        tagsList: [],
        collapse: false,
        islogin: false,
        isAdministrator: false,
        username: "未登录",
        user_id: "",
        role: 2,
    },
    mutations: {
        login(state, username) {
            state.islogin = true
            state.username = username
        },
        changeName(state,username){
            state.username = username
        },
        storeId(state, userid) {
            state.user_id = userid
        },
        loginout(state) {
            state.islogin = false
            state.username = "未登录"
            state.user_id = ""
            state.tagsList=[]
        },
        setRole(state, role) {
            state.role = role
        },
        delTagsItem(state, data) {
            state
                .tagsList
                .splice(data.index, 1);
        },
        setTagsItem(state, data) {
            state
                .tagsList
                .push(data)
        },
        clearTags(state) {
            state.tagsList = []
        },
        closeTagsOther(state, data) {
            state.tagsList = data;
        },
        closeCurrentTag(state, data) {
            for (let i = 0, len = state.tagsList.length; i < len; i++) {
                const item = state.tagsList[i];
                if (item.path === data.$route.fullPath) {
                    if (i < len - 1) {
                        data
                            .$router
                            .push(state.tagsList[i + 1].path);
                    } else if (i > 0) {
                        data
                            .$router
                            .push(state.tagsList[i - 1].path);
                    } else {
                        data
                            .$router
                            .push("/");
                    }
                    state
                        .tagsList
                        .splice(i, 1);
                    break;
                }
            }
        },
        // 侧边栏折叠
        hadndleCollapse(state, data) {
            state.collapse = data;
        }
    },
    actions: {},
    modules: {},
    plugins: [createPersistedState()]
})