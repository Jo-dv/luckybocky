import { createHashRouter } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import MainPage from "./pages/MainPage";
import AccountPage from "./pages/AccountPage";
import MyMessagePage from "./pages/MyArticlePage";
import SelectDecoPage from "./pages/SelectDecoPage";
import WritePage from "./pages/WritePage";
import CallBack from "./pages/CallBack";
import ErrorPage from "./pages/ErrorPage";
import JoinPage from "./pages/JoinPage";
import CheckUser from "./components/CheckUser";

const router = createHashRouter([
  {
    element: <CheckUser />,
    children: [
      {
        path: "/",
        element: <LoginPage />,
      },
      {
        path: "/:address",
        element: <MainPage />,
      },
      {
        path: "/account",
        element: <AccountPage />,
      },
      {
        path: "/my-message",
        element: <MyMessagePage />,
      },
      {
        path: "/select-deco",
        element: <SelectDecoPage />,
      },
      {
        path: "/write",
        element: <WritePage />,
      },
      {
        path: "/callback",
        element: <CallBack />,
      },
      {
        path: "/error",
        element: <ErrorPage />,
      },
      {
        path: "/join",
        element: <JoinPage />,
      },
    ],
  },
]);

export default router;
