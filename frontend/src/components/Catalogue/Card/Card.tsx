import React from 'react';
import styles from "./Card.module.scss";
import TypeTag from "../../Text/Type/TypeTag";

type TitleProps = {
    imageUrl: string;
    ndex: number;
    name: string;
    type1: string;
    type2?: string;
};

const Title: React.FC<TitleProps> = ({imageUrl, ndex, name, type1, type2}) => {
    return (
        <div className={styles.card}>
            <p className={styles.ndex}>
                    N°{String(ndex).padStart(4, '0')}
            </p>
            <div className={styles.cardImage}>
                <img src={imageUrl} alt="Pokèmon"/>
            </div>
            <div className={styles.cardBody}>
                <h2 className={styles.name}>
                    {name}
                </h2>
                <div className={styles.types}>
                    <TypeTag type={type1}/>
                    {type2 && <TypeTag type={type2} />}
                </div>
            </div>
        </div>
    );
};

export default Title;