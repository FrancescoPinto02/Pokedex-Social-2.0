import React from 'react';
import styles from "./Title.module.scss";

type TitleProps = {
    text: string;
    backgroundColor?: string;
    color?: string;
};

const Title: React.FC<TitleProps> = ({text, backgroundColor="transparent", color="black" }) => {
    return (
        <div className={styles.container} style={{backgroundColor}}>
            <h1 style={{color}}>
                {text}
            </h1>
        </div>
    );
};

export default Title;