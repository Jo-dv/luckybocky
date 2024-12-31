/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["ChosunContennial", "SFHambaknun", "KyunggiThousandYear", "Nanum", "sans-serif"], // Nanum 폰트를 추가
      },
    },
  },
  plugins: [],
};
