import React, { useEffect, useState } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import { checkLogin } from "../api/AuthApi";

const CheckUser = () => {
  const [initialized, setInitialized] = useState(false); // 초기화 상태
  const navigate = useNavigate();

  const init = async () => {
    if ((await checkLogin()) === 1) navigate("/join");
    setInitialized(true);
  };

  useEffect(() => {
    init();
  }, []);

  if (initialized) {
    return <Outlet />;
  }
};

export default CheckUser;
